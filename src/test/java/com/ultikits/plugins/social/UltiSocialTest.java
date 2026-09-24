package com.ultikits.plugins.social;

import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.ultitools.annotations.ConfigEntity;
import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UltiSocial Main Class Tests")
class UltiSocialTest {

    @Test
    @DisplayName("registerSelf should return true")
    void registerSelf() throws Exception {
        UltiSocial plugin = mock(UltiSocial.class);
        lenient().when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
        PluginLogger logger = mock(PluginLogger.class);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.registerSelf()).thenCallRealMethod();

        boolean result = plugin.registerSelf();

        assertThat(result).isTrue();
        verify(logger).info("UltiSocial v1.1.0 has been enabled!");
    }

    /**
     * UltiTools 6.3.0 makes {@code unregisterSelf()} and {@code reloadSelf()} final template
     * methods that always run the framework's own steps (config reload and language refresh on
     * reload; command and listener unregistration on unload) around the module's hooks. This
     * module's former overrides only logged a literal line and never called {@code super}, so
     * {@code /ul reload UltiSocial} reported success without re-reading anything
     * (UltiKits/UltiSocial#13). They are deleted outright; this test pins that neither is
     * declared again.
     */
    @Test
    @DisplayName("declares neither framework lifecycle template method (UltiKits/UltiSocial#13)")
    void declaresNeitherLifecycleTemplateMethod() {
        List<String> declared = new ArrayList<>();
        for (Method method : UltiSocial.class.getDeclaredMethods()) {
            declared.add(method.getName());
        }

        assertThat(declared).doesNotContain("unregisterSelf", "reloadSelf");
    }

    @Test
    @DisplayName("supported should return zh and en")
    void supported() throws Exception {
        UltiSocial plugin = mock(UltiSocial.class);
        lenient().when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
        when(plugin.supported()).thenCallRealMethod();

        List<String> langs = plugin.supported();

        assertThat(langs).containsExactly("zh", "en");
    }

    @Nested
    @DisplayName("Annotation Tests")
    class AnnotationTests {

        @Test
        @DisplayName("should have @UltiToolsModule annotation")
        void shouldHaveModuleAnnotation() {
            assertThat(UltiSocial.class.isAnnotationPresent(
                com.ultikits.ultitools.annotations.UltiToolsModule.class
            )).isTrue();
        }

        @Test
        @DisplayName("should scan correct packages")
        void shouldScanCorrectPackages() {
            com.ultikits.ultitools.annotations.UltiToolsModule annotation =
                UltiSocial.class.getAnnotation(
                    com.ultikits.ultitools.annotations.UltiToolsModule.class
                );

            assertThat(annotation.scanBasePackages()).contains("com.ultikits.plugins.social");
        }
    }

    /**
     * UltiKits/UltiSocial#15. {@code RemovedConfigKeysTest} guards the check's predicate; these
     * tests guard its WIRING, which is a separate claim: with the call sites deleted the predicate
     * tests stay green, and a server with a leftover key prints nothing, exactly like a server
     * without one. Both entry points are covered -- module enable and every reload of the module --
     * because a guard on one would leave the other free to lose its call silently.
     * <p>
     * The operator's file is reached through {@code operatorConfigFile()}, a package-private seam:
     * the framework's {@code getConfigFile} is {@code protected final}, so this package can neither
     * call nor stub it, and a mocked plugin returns {@code null} from it.
     */
    @Nested
    @DisplayName("the removed-key check is actually called (UltiKits/UltiSocial#15)")
    class RemovedKeyCheckWiring {

        private static final String FILE_WITH_THE_REMOVED_KEY =
                "notifications:\n  friend_online: true\n  friend_join_world: false\n";

        private static final String FILE_WITHOUT_THE_REMOVED_KEY =
                "notifications:\n  friend_online: true\n";

        private PluginLogger logger;

        private UltiSocial pluginReading(File dir, String body) throws IOException {
            File file = new File(dir, "social.yml");
            Files.write(file.toPath(), body.getBytes(StandardCharsets.UTF_8));

            UltiSocial plugin = mock(UltiSocial.class);

            lenient().when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
            logger = mock(PluginLogger.class);
            when(plugin.getLogger()).thenReturn(logger);
            when(plugin.operatorConfigFile()).thenReturn(file);
            return plugin;
        }

        private List<String> warnings() {
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(logger, atLeast(0)).warn(captor.capture());
            return captor.getAllValues();
        }

        @Test
        @DisplayName("POSITIVE CONTROL: enabling the module warns about the leftover key")
        void registerSelfWarns(@TempDir File dir) throws IOException {
            UltiSocial plugin = pluginReading(dir, FILE_WITH_THE_REMOVED_KEY);
            when(plugin.registerSelf()).thenCallRealMethod();

            assertThat(plugin.registerSelf()).isTrue();

            assertThat(warnings()).hasSize(1);
            assertThat(warnings().get(0)).contains("notifications.friend_join_world");
        }

        @Test
        @DisplayName("POSITIVE CONTROL: reloading the module warns about the leftover key")
        void onReloadWarns(@TempDir File dir) throws IOException {
            UltiSocial plugin = pluginReading(dir, FILE_WITH_THE_REMOVED_KEY);
            doCallRealMethod().when(plugin).onReload();

            plugin.onReload();

            assertThat(warnings()).hasSize(1);
            assertThat(warnings().get(0)).contains("notifications.friend_join_world");
        }

        @Test
        @DisplayName("neither entry point warns when the file holds no removed key")
        void neitherWarnsOnACleanFile(@TempDir File dir) throws IOException {
            // Paired with the two controls above: same entry points, same file, the one key
            // taken out and nothing else changed.
            UltiSocial onEnable = pluginReading(dir, FILE_WITHOUT_THE_REMOVED_KEY);
            when(onEnable.registerSelf()).thenCallRealMethod();
            assertThat(onEnable.registerSelf()).isTrue();
            assertThat(warnings()).isEmpty();

            UltiSocial onReload = pluginReading(dir, FILE_WITHOUT_THE_REMOVED_KEY);
            doCallRealMethod().when(onReload).onReload();
            onReload.onReload();
            assertThat(warnings()).isEmpty();
        }

        @Test
        @DisplayName("the check reads the file SocialConfig binds, taken from its @ConfigEntity and nowhere else")
        void readsTheFileSocialConfigBinds() {
            // Every other test here stubs operatorConfigFile(), so if the path the check resolves
            // ever drifted from the file SocialConfig binds, the production check would read a file
            // that does not exist, return silently, and look exactly like a server with no leftover
            // key. The framework constructs SocialConfig with its @ConfigEntity value, so that value
            // is the path the check must use.
            UltiSocial plugin = mock(UltiSocial.class);
            lenient().when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
            when(plugin.operatorConfigPath()).thenCallRealMethod();

            String declared = SocialConfig.class.getAnnotation(ConfigEntity.class).value();

            assertThat(declared).isEqualTo("config/social.yml");
            assertThat(plugin.operatorConfigPath()).isEqualTo(declared);
        }

        @Test
        @DisplayName("a failure inside the check never costs the module its enable or its reload")
        void aFailingCheckNeverFailsEnableOrReload() {
            // The check is advisory. Simulated with the file lookup itself failing: the module must
            // still enable and reload, and the failure is reported once per entry point.
            UltiSocial plugin = mock(UltiSocial.class);
            lenient().when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
            logger = mock(PluginLogger.class);
            when(plugin.getLogger()).thenReturn(logger);
            when(plugin.operatorConfigFile())
                    .thenThrow(new UncheckedIOException(new IOException("disk unavailable")));
            when(plugin.registerSelf()).thenCallRealMethod();
            doCallRealMethod().when(plugin).onReload();

            assertThat(plugin.registerSelf()).isTrue();
            assertThatCode(plugin::onReload).doesNotThrowAnyException();

            ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).warn(any(Throwable.class), messages.capture());
            assertThat(messages.getAllValues())
                    .allSatisfy(m -> assertThat(m).contains("removed").contains("social.yml"));
        }
    }
}
