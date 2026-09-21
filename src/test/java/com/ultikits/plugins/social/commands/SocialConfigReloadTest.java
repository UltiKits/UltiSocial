package com.ultikits.plugins.social.commands;

import com.ultikits.plugins.social.UltiSocial;
import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.abstracts.AbstractConfigEntity;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.interfaces.DataOperator;
import com.ultikits.ultitools.interfaces.Query;
import com.ultikits.ultitools.manager.ConfigManager;
import com.ultikits.ultitools.services.TeleportService;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UltiKits/UltiSocial#13: {@code /ul reload UltiSocial} re-reads {@code config/social.yml} through
 * {@link ConfigManager#reloadConfigs}, which calls {@code init(plugin)} again on the SAME
 * {@link SocialConfig} instance that was registered at load. This test drives a real
 * {@link ConfigManager} through {@code register} and {@code reloadConfigs}, asserts the registered
 * instance is the one {@link FriendService} holds, and proves {@code /friend tp} observes the reload
 * on its very next invocation, i.e. nothing caches {@code tp_to_friend.enabled}. Backs the
 * {@code ultisocial.lifecycle.reload} checklist row.
 * <p>
 * One framework fact remains assumed rather than asserted: {@code PluginManager} injects the
 * {@code ConfigManager}'s registered instance into the container. A regression guard, not a
 * reproduction of #13: that defect was the module's override never reaching
 * {@code reloadConfigs}, which a unit test cannot exercise without the framework's managers; the
 * real-machine row covers it end to end.
 */
@DisplayName("/friend tp observes an in-place SocialConfig reload (UltiKits/UltiSocial#13)")
class SocialConfigReloadTest {

    @TempDir
    Path moduleFolder;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    @Test
    @DisplayName("disabling tp_to_friend.enabled and reloading through ConfigManager refuses the next /friend tp")
    @SuppressWarnings("unchecked")
    void inPlaceReloadOfTpToFriendEnabledIsObserved() throws Exception {
        File configFile = moduleFolder.resolve("config").resolve("social.yml").toFile();
        assertThat(configFile.getParentFile().mkdirs()).isTrue();
        write(configFile, "tp_to_friend:\n  enabled: true\n");

        UltiToolsPlugin configPlugin = mock(UltiSocial.class, CALLS_REAL_METHODS);
        setField(UltiToolsPlugin.class, configPlugin, "resourceFolderPath", moduleFolder.toString());

        SocialConfig config = new SocialConfig("config/social.yml");
        ConfigManager configManager = new ConfigManager();
        configManager.register(configPlugin, config);
        Map<String, AbstractConfigEntity> registered = configManager.getAllConfigEntities(configPlugin);
        assertThat(registered.values()).singleElement().isSameAs(config);
        assertThat(config.isTpToFriendEnabled()).isTrue();

        DataOperator<FriendshipData> dataOperator = mock(DataOperator.class);
        Query<FriendshipData> query = mock(Query.class);
        when(dataOperator.query()).thenReturn(query);
        when(query.where("player_uuid")).thenReturn(query);
        when(query.eq(anyString())).thenReturn(query);
        when(query.list()).thenAnswer(inv -> new ArrayList<FriendshipData>());

        FriendService service = new FriendService();
        setField(FriendService.class, service, "config", config);
        setField(FriendService.class, service, "dataOperator", dataOperator);
        assertThat(registered.values()).singleElement().isSameAs(service.getConfig());
        FriendCommand command = new FriendCommand(service, mock(TeleportService.class));
        Player player = UltiSocialTestHelper.createMockPlayer("Tester1", UUID.randomUUID());

        command.teleportToFriend(player, "NoSuchFriend01");
        verify(player).sendMessage(contains("不是你的好友"));
        verify(player, never()).sendMessage(contains("已禁用"));

        write(configFile, "tp_to_friend:\n  enabled: false\n");
        configManager.reloadConfigs(configPlugin);

        command.teleportToFriend(player, "NoSuchFriend01");
        verify(player).sendMessage(contains("已禁用"));
    }

    private static void write(File file, String content) throws Exception {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // simulates @Autowired injection and points the config folder at a temp directory
    private static void setField(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
