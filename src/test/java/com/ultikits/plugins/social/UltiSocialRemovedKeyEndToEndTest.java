package com.ultikits.plugins.social;

import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;
import com.ultikits.ultitools.manager.ConfigManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * UltiKits/UltiSocial#15, end to end. {@code UltiSocialTest$RemovedKeyCheckWiring} stubs
 * {@code operatorConfigFile()}, so it proves the check is called but not that it reads the file the
 * framework actually manages. Here nothing about the file is stubbed: the module's folder is a temp
 * directory, a real {@link ConfigManager} registers the real {@link SocialConfig} against the
 * operator's {@code config/social.yml} (loading it and writing the missing declared defaults, as it
 * does on a real server before {@code registerSelf()} runs), and then the real {@code registerSelf()}
 * and {@code onReload()} resolve the file themselves. Only the logger is replaced, to capture what
 * would reach the console.
 * <p>
 * This also pins, as a test, the framework fact the warning depends on: loading and saving the
 * configuration keeps an undeclared key in the operator's file rather than dropping it. If that ever
 * stopped being true the warning would be pointless, and the first assertion below would say so.
 */
@DisplayName("the removed-key warning reads the operator's real social.yml (UltiKits/UltiSocial#15)")
class UltiSocialRemovedKeyEndToEndTest {

    /** An upgraded server's file: the removed key is present, and some declared keys are missing. */
    private static final String UPGRADED_FILE =
            "max_friends: 50\n"
            + "notifications:\n"
            + "  friend_online: true\n"
            + "  friend_join_world: true\n";

    /** The same file with only the removed key taken out. */
    private static final String CLEAN_FILE =
            "max_friends: 50\n"
            + "notifications:\n"
            + "  friend_online: true\n";

    @TempDir
    Path moduleFolder;

    private PluginLogger logger;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    /** A module whose folder is the temp directory, with the operator's file registered for real. */
    private UltiSocial moduleWithOperatorFile(String body) throws Exception {
        File file = operatorFile();
        assertThat(file.getParentFile().mkdirs()).isTrue();
        Files.write(file.toPath(), body.getBytes(StandardCharsets.UTF_8));

        UltiSocial plugin = mock(UltiSocial.class, CALLS_REAL_METHODS);
        setField(UltiToolsPlugin.class, plugin, "resourceFolderPath", moduleFolder.toString());
        new ConfigManager().register(plugin, new SocialConfig("config/social.yml"));

        logger = mock(PluginLogger.class);
        doReturn(logger).when(plugin).getLogger();
        // The warning's text comes from the language file (English here); the start-up step that writes the
        // title and messages in the server's language finds no configuration bean on this bare plugin and does nothing.
        doAnswer(CatalogueText.answer("en")).when(plugin).i18n(anyString());
        lenient().doReturn(null).when(plugin).getConfig(SocialConfig.class);
        return plugin;
    }

    private File operatorFile() {
        return moduleFolder.resolve("config").resolve("social.yml").toFile();
    }

    private List<String> warnings() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, atLeast(0)).warn(captor.capture());
        return captor.getAllValues();
    }

    @Test
    @DisplayName("POSITIVE CONTROL: after the framework loads the upgraded file, enabling the module warns once, naming that file")
    void enablingWarnsAboutTheRealFile() throws Exception {
        UltiSocial plugin = moduleWithOperatorFile(UPGRADED_FILE);

        YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(operatorFile());
        assertThat(onDisk.contains("tp_to_friend.enabled"))
                .as("control: the framework loaded the file and wrote a missing declared default")
                .isTrue();
        assertThat(onDisk.contains("notifications.friend_join_world"))
                .as("the framework keeps the undeclared key in the operator's file")
                .isTrue();

        assertThat(plugin.registerSelf()).isTrue();

        assertThat(warnings()).hasSize(1);
        assertThat(warnings().get(0))
                .contains("'notifications.friend_join_world'")
                .contains(operatorFile().getPath());
    }

    @Test
    @DisplayName("POSITIVE CONTROL: reloading the module warns once, naming that file")
    void reloadingWarnsAboutTheRealFile() throws Exception {
        UltiSocial plugin = moduleWithOperatorFile(UPGRADED_FILE);

        plugin.onReload();

        assertThat(warnings()).hasSize(1);
        assertThat(warnings().get(0))
                .contains("'notifications.friend_join_world'")
                .contains(operatorFile().getPath());
    }

    @Test
    @DisplayName("the same flow on the same file without the removed key warns at neither entry point")
    void cleanFileWarnsNowhere() throws Exception {
        UltiSocial plugin = moduleWithOperatorFile(CLEAN_FILE);

        assertThat(plugin.registerSelf()).isTrue();
        plugin.onReload();

        assertThat(warnings()).isEmpty();
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // points the module's config folder at a temp directory
    private static void setField(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
