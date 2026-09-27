package com.ultikits.plugins.social.config;

import com.ultikits.plugins.social.UltiSocial;
import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.manager.ConfigManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * What a server that installs this module for the first time gets in {@code config/social.yml}.
 * <p>
 * The module ships no {@code social.yml} resource: the framework writes the file from
 * {@link SocialConfig}'s {@code @ConfigEntry} defaults the first time the module boots, so the set of
 * keys in a fresh file IS the module's declared configuration surface. These tests drive a real
 * {@link ConfigManager} against an empty module folder and read back the file it writes.
 * <p>
 * The file holds the 17 settings {@link SocialConfig} declares, all read by the module. Two more,
 * {@code messages.player_blocked} and {@code messages.player_unblocked}, were declared but read by
 * nothing and are removed (UltiKits/UltiSocial#23); the replies to {@code /friend block} and
 * {@code /friend unblock} come from the language file. The framework writes the eleven message and
 * title settings with the text each shipped with in every earlier version; the module then writes them
 * in the server's language when it starts (maintainer decision 2026-09-25; {@code SocialConfigTextTest}).
 * <p>
 * UltiKits/UltiSocial#15: {@code notifications.friend_join_world} declared a "notify when a friend
 * joins your world" feature that was never implemented; per the maintainer's 2026-09-22 decision the
 * setting is removed rather than the feature built (tracked as a request in UltiKits/UltiSocial#21).
 * The assertion is written as what a fresh file must hold, not as what the class must lack, so it
 * runs against the code before the removal and fails there.
 */
@DisplayName("a fresh config/social.yml holds exactly the settings SocialConfig declares (UltiKits/UltiSocial#15)")
class SocialConfigFreshFileTest {

    /**
     * Every key a fresh file must hold, in the order {@link SocialConfig} declares them.
     */
    private static final List<String> EXPECTED_KEYS = Arrays.asList(
            "max_friends",
            "request_timeout",
            "notifications.friend_online",
            "notifications.friend_offline",
            "tp_to_friend.enabled",
            "tp_to_friend.cooldown",
            "gui_title",
            "messages.friend_added",
            "messages.friend_removed",
            "messages.friend_online",
            "messages.friend_offline",
            "messages.request_sent",
            "messages.request_received",
            "messages.request_denied",
            "messages.max_friends_reached",
            "messages.already_friends",
            "messages.blocked");

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

    private YamlConfiguration freshFile() throws Exception {
        UltiToolsPlugin plugin = mock(UltiSocial.class, CALLS_REAL_METHODS);
        setField(UltiToolsPlugin.class, plugin, "resourceFolderPath", moduleFolder.toString());

        new ConfigManager().register(plugin, new SocialConfig("config/social.yml"));

        File written = moduleFolder.resolve("config").resolve("social.yml").toFile();
        assertThat(written).as("control: the framework wrote a fresh file").isFile();
        return YamlConfiguration.loadConfiguration(written);
    }

    @Test
    @DisplayName("POSITIVE CONTROL: the two notification switches this module reads are written")
    void writesTheNotificationSwitchesThatAreRead() throws Exception {
        YamlConfiguration yaml = freshFile();

        assertThat(yaml.contains("notifications.friend_online")).isTrue();
        assertThat(yaml.contains("notifications.friend_offline")).isTrue();
    }

    @Test
    @DisplayName("the notifications section holds only the online and offline switches, never friend_join_world")
    void notificationsHoldOnlyOnlineAndOffline() throws Exception {
        YamlConfiguration yaml = freshFile();

        assertThat(yaml.contains("notifications.friend_join_world")).isFalse();
        assertThat(yaml.getConfigurationSection("notifications").getKeys(false))
                .containsExactly("friend_online", "friend_offline");
    }

    @Test
    @DisplayName("the whole file holds exactly the 17 settings SocialConfig declares and FEATURES.md lists, and no other")
    void holdsExactlyTheDocumentedKeys() throws Exception {
        YamlConfiguration yaml = freshFile();

        Set<String> leaves = new java.util.LinkedHashSet<String>();
        for (String key : yaml.getKeys(true)) {
            if (!yaml.isConfigurationSection(key)) {
                leaves.add(key);
            }
        }
        assertThat(leaves).containsExactlyInAnyOrderElementsOf(EXPECTED_KEYS);
        assertThat(leaves).hasSize(17);
    }

    @Test
    @DisplayName("the title and the ten messages are written with the text each shipped with, which is this jar's Chinese text, never blank")
    void textSettingsAreWrittenWithTheirShippedText() throws Exception {
        YamlConfiguration yaml = freshFile();

        java.util.Map<String, String> catalogueKey = new java.util.LinkedHashMap<String, String>();
        catalogueKey.put("gui_title", "gui_friend_list");
        for (String key : EXPECTED_KEYS) {
            if (key.startsWith("messages.")) {
                catalogueKey.put(key, key.substring("messages.".length()));
            }
        }
        assertThat(catalogueKey).hasSize(11);
        for (java.util.Map.Entry<String, String> e : catalogueKey.entrySet()) {
            assertThat(yaml.getString(e.getKey())).as(e.getKey())
                    .isNotBlank()
                    .isEqualTo(com.ultikits.plugins.social.i18n.CatalogueText.text("zh", e.getValue()));
        }
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // points the module's config folder at a temp directory
    private static void setField(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
