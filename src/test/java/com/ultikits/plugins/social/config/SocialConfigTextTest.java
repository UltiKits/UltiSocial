package com.ultikits.plugins.social.config;

import com.ultikits.plugins.social.UltiSocial;
import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.gui.FriendListGUI;
import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.plugins.social.i18n.SocialSeams;
import com.ultikits.plugins.social.listener.SocialListener;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.annotations.ConfigEntry;
import com.ultikits.ultitools.annotations.config.NotEmpty;
import com.ultikits.ultitools.interfaces.ConfigChangeListener;
import com.ultikits.ultitools.interfaces.DataOperator;
import com.ultikits.ultitools.interfaces.Query;
import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code config/social.yml} holds the friend-list title and every friend message in the server's
 * language, and the module shows exactly what the file holds (maintainer decision 2026-09-25;
 * UltiKits/UltiSocial#14). A value that is still built-in text -- any language's text from this jar,
 * or the default an earlier version shipped -- follows {@code language} at enable and on reload, in
 * both directions; anything else is the operator's and is kept byte for byte. Every case runs the
 * framework's real {@code AbstractConfigEntity#init} on a temporary folder, the module's real
 * {@code registerSelf()} and {@code onReload()}, and answers from the module's real catalogues.
 */
@DisplayName("social.yml holds the friend-list title and messages in the server's language (UltiKits/UltiSocial#14)")
class SocialConfigTextTest {

    /** One text setting: its field, its path in social.yml, its catalogue key, its shipped default. */
    private static final class Setting {
        final String field;
        final String path;
        final String key;
        final String shipped;

        Setting(String field, String path, String key, String shipped) {
            this.field = field;
            this.path = path;
            this.key = key;
            this.shipped = shipped;
        }

        String text(String code) {
            return CatalogueText.text(code, key);
        }

        String getter() {
            return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        }
    }

    /** The 11 settings, with the one default each shipped in every earlier version (this module's history). */
    private static final List<Setting> SETTINGS = Arrays.asList(
            new Setting("guiTitle", "gui_title", "gui_friend_list", "&6好友列表 &7({COUNT}/{MAX})"),
            new Setting("friendAddedMessage", "messages.friend_added", "friend_added", "&a你和 {PLAYER} 成为了好友！"),
            new Setting("friendRemovedMessage", "messages.friend_removed", "friend_removed", "&c你已删除好友 {PLAYER}"),
            new Setting("friendOnlineMessage", "messages.friend_online", "friend_online", "&a你的好友 {PLAYER} 上线了！"),
            new Setting("friendOfflineMessage", "messages.friend_offline", "friend_offline", "&7你的好友 {PLAYER} 下线了"),
            new Setting("requestSentMessage", "messages.request_sent", "request_sent", "&a已向 {PLAYER} 发送好友请求！"),
            new Setting("requestReceivedMessage", "messages.request_received", "request_received",
                    "&e{PLAYER} 想和你成为好友！输入 /friend accept {PLAYER} 接受"),
            new Setting("requestDeniedMessage", "messages.request_denied", "request_denied", "&c已拒绝 {PLAYER} 的好友请求"),
            new Setting("maxFriendsMessage", "messages.max_friends_reached", "max_friends_reached", "&c你的好友数量已达上限！"),
            new Setting("alreadyFriendsMessage", "messages.already_friends", "already_friends", "&c你已经和 {PLAYER} 是好友了！"),
            new Setting("blockedMessage", "messages.blocked", "blocked", "&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系"));

    /**
     * The fields that carried {@code @NotEmpty} at origin/master and still exist: the 11 above. Master
     * also had it on {@code playerBlockedMessage} and {@code playerUnblockedMessage}, two settings nothing
     * read, which this branch deleted (UltiKits/UltiSocial#23); they cannot carry an annotation any more.
     */
    private static final Set<String> NOT_EMPTY_AT_MASTER = new TreeSet<>();

    /** Fields of origin/master's SocialConfig that no longer exist, so are left out of the comparison. */
    private static final List<String> DELETED_SINCE_MASTER = Arrays.asList("playerBlockedMessage", "playerUnblockedMessage");

    static {
        for (Setting s : SETTINGS) {
            NOT_EMPTY_AT_MASTER.add(s.field);
        }
    }

    private static final String[] LANGUAGES = {"en", "zh"};

    private static final Pattern CJK = Pattern.compile("[\\u4e00-\\u9fff]");

    @TempDir
    Path tempDir;

    private final String[] language = {"en"};

    private final PluginLogger logger = mock(PluginLogger.class);

    /** Catalogue texts an operator changed in the extracted language file on disk, answered by i18n first. */
    private final Map<String, String> diskOverrides = new LinkedHashMap<>();

    /** The configuration the module double returns from {@code getConfig(SocialConfig.class)}. */
    private SocialConfig current;

    private UltiSocial plugin;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
        plugin = moduleDouble();
    }

    @AfterEach
    void tearDown() throws Exception {
        current = null;
        UltiSocialTestHelper.tearDown();
    }

    @Test
    @DisplayName("the catalogues give each setting English text under en and exactly its shipped default under zh")
    void catalogueTexts() {
        for (Setting s : SETTINGS) {
            assertThat(s.text("zh")).as(s.field).isEqualTo(s.shipped);
            assertThat(s.text("en")).as(s.field).doesNotMatch("(?s).*" + CJK.pattern() + ".*");
        }
    }

    @Test
    @DisplayName("fresh start under en: social.yml holds every setting's English text, and each getter returns the file's value")
    void freshStartEnglish() throws Exception {
        language[0] = "en";
        SocialConfig config = spy(load());

        start(config);

        YamlConfiguration disk = onDisk();
        for (Setting s : SETTINGS) {
            assertThat(disk.getString(s.path)).as(s.path).isEqualTo(s.text("en"));
            assertThat(get(config, s)).as(s.field).isEqualTo(disk.getString(s.path));
        }
        verify(config, times(1)).save();
    }

    @Test
    @DisplayName("fresh start under zh: social.yml holds every setting's Chinese text, which is its shipped default, and the module writes nothing")
    void freshStartChinese() throws Exception {
        language[0] = "zh";
        SocialConfig config = spy(load());
        byte[] afterFramework = bytes();

        start(config);

        YamlConfiguration disk = onDisk();
        for (Setting s : SETTINGS) {
            assertThat(disk.getString(s.path)).as(s.path).isEqualTo(s.shipped);
            assertThat(get(config, s)).as(s.field).isEqualTo(s.shipped);
        }
        verify(config, never()).save();
        assertThat(bytes()).isEqualTo(afterFramework);
    }

    @Test
    @DisplayName("every built-in text in the file (shipped default, jar en text, jar zh text) is replaced with the current language's text and saved, under en and zh")
    void everyTrackedValueFollowsTheLanguage() throws Exception {
        for (String code : LANGUAGES) {
            for (String member : new String[] {"shipped", "en", "zh"}) {
                language[0] = code;
                Map<String, String> values = new LinkedHashMap<>();
                for (Setting s : SETTINGS) {
                    values.put(s.path, "shipped".equals(member) ? s.shipped : s.text(member));
                }
                write(values);
                SocialConfig config = spy(load());

                start(config);

                YamlConfiguration disk = onDisk();
                for (Setting s : SETTINGS) {
                    String what = "language " + code + ", file held the " + member + " text of " + s.path;
                    assertThat(disk.getString(s.path)).as(what).isEqualTo(s.text(code));
                    assertThat(get(config, s)).as(what).isEqualTo(s.text(code));
                }
                boolean alreadyCurrent = member.equals(code) || ("shipped".equals(member) && "zh".equals(code));
                verify(config, times(alreadyCurrent ? 0 : 1)).save();
            }
        }
    }

    @Test
    @DisplayName("an upgraded file holding the shipped defaults reads exactly the English text under en (pinned, not read from the catalogue)")
    void upgradedFileReadsExactEnglish() throws Exception {
        language[0] = "en";
        Map<String, String> values = new LinkedHashMap<>();
        for (Setting s : SETTINGS) {
            values.put(s.path, s.shipped);
        }
        write(values);
        SocialConfig config = spy(load());

        start(config);

        YamlConfiguration disk = onDisk();
        assertThat(disk.getString("gui_title")).isEqualTo("&6Friend List &7({COUNT}/{MAX})");
        assertThat(disk.getString("messages.friend_added")).isEqualTo("&aYou and {PLAYER} are now friends!");
        assertThat(disk.getString("messages.request_received"))
                .isEqualTo("&e{PLAYER} wants to be your friend! Type /friend accept {PLAYER} to accept");
        assertThat(disk.getString("messages.blocked"))
                .isEqualTo("&cCannot perform friend operations with {PLAYER} due to blacklist");
        assertThat(config.getFriendAddedMessage()).isEqualTo("&aYou and {PLAYER} are now friends!");
        verify(config, times(1)).save();
    }

    @Test
    @DisplayName("a customised value, or built-in text changed by one character, is kept byte for byte under both languages and the file is not rewritten")
    void customisedValuesAreKept() throws Exception {
        for (String code : LANGUAGES) {
            for (String variant : new String[] {"shipped!", "en!", "zh!", "own"}) {
                language[0] = code;
                Map<String, String> values = new LinkedHashMap<>();
                for (Setting s : SETTINGS) {
                    String v;
                    if ("shipped!".equals(variant)) {
                        v = s.shipped + "!";
                    } else if ("en!".equals(variant)) {
                        v = s.text("en") + " ";
                    } else if ("zh!".equals(variant)) {
                        v = "&f" + s.text("zh").substring(2);
                    } else {
                        v = "&dOperator text for " + s.field;
                    }
                    values.put(s.path, v);
                }
                write(values);
                SocialConfig config = spy(load());
                byte[] before = bytes();

                start(config);

                assertThat(bytes()).as(code + " " + variant).isEqualTo(before);
                for (Setting s : SETTINGS) {
                    assertThat(get(config, s)).as(code + " " + variant + " " + s.field).isEqualTo(values.get(s.path));
                }
                verify(config, never()).save();
            }
        }
    }

    @Test
    @DisplayName("a second enable with the same language writes nothing")
    void secondEnableWritesNothing() throws Exception {
        for (String code : LANGUAGES) {
            language[0] = code;
            Map<String, String> values = new LinkedHashMap<>();
            for (Setting s : SETTINGS) {
                values.put(s.path, s.shipped);
            }
            write(values);
            start(load());
            byte[] afterFirst = bytes();

            SocialConfig second = spy(load());
            start(second);

            assertThat(bytes()).as(code).isEqualTo(afterFirst);
            verify(second, never()).save();
        }
    }

    @Test
    @DisplayName("onReload() after a language switch rewrites every setting in the new language, in both directions")
    void reloadFollowsALanguageSwitchBothWays() throws Exception {
        for (String[] direction : new String[][] {{"en", "zh"}, {"zh", "en"}}) {
            language[0] = direction[0];
            Files.deleteIfExists(file().toPath());
            SocialConfig config = load();
            start(config);

            language[0] = direction[1];
            config.init(plugin);
            reload();

            YamlConfiguration disk = onDisk();
            for (Setting s : SETTINGS) {
                String what = direction[0] + " -> " + direction[1] + ": " + s.path;
                assertThat(disk.getString(s.path)).as(what).isEqualTo(s.text(direction[1]));
                assertThat(get(config, s)).as(what).isEqualTo(s.text(direction[1]));
            }
        }
    }

    @Test
    @DisplayName("no configuration change listener rewrites the text (the framework fires them before it reloads the language)")
    void changeListenersDoNotMaterialize() throws Exception {
        language[0] = "en";
        SocialConfig config = load();
        start(config);
        byte[] before = bytes();

        language[0] = "zh";
        // This module registers no change listener, and the framework registers none for it, so nothing
        // can write the file before the framework rebuilds the language; pinned so that adding one is seen.
        assertThat(config.getChangeListeners()).isEmpty();
        for (ConfigChangeListener listener : new ArrayList<>(config.getChangeListeners())) {
            listener.onConfigReload(config);
        }

        assertThat(bytes()).isEqualTo(before);
        assertThat(config.getFriendAddedMessage()).isEqualTo(SETTINGS.get(1).text("en"));
    }

    @Test
    @DisplayName("an operator-edited language file on disk does not widen what counts as built-in text")
    void diskCatalogueDoesNotWidenTheTrackedSet() throws Exception {
        Path lang = Files.createDirectories(tempDir.resolve("lang"));
        StringBuilder yml = new StringBuilder();
        for (Setting s : SETTINGS) {
            yml.append(s.key).append(": \"Edited ").append(s.key).append("\"\n");
            diskOverrides.put(s.key, "Edited " + s.key);
        }
        for (String code : LANGUAGES) {
            Files.write(lang.resolve(code + ".yml"), yml.toString().getBytes(StandardCharsets.UTF_8));
        }
        for (String code : LANGUAGES) {
            language[0] = code;
            Map<String, String> values = new LinkedHashMap<>();
            for (Setting s : SETTINGS) {
                values.put(s.path, "Edited " + s.key);
            }
            write(values);
            SocialConfig config = spy(load());

            start(config);

            for (Setting s : SETTINGS) {
                assertThat(onDisk().getString(s.path)).as(code + " " + s.path).isEqualTo(values.get(s.path));
            }
            verify(config, never()).save();
        }
    }

    @Test
    @DisplayName("an operator's edit of the extracted language file is not written into social.yml, so each value keeps following a language switch (the text source decision of 2026-09-25)")
    void diskCatalogueEditDoesNotReachTheFile() throws Exception {
        for (Setting s : SETTINGS) {
            diskOverrides.put(s.key, "Edited " + s.key);
        }
        // control: the module's i18n really answers the edited text
        assertThat(plugin.i18n(SETTINGS.get(0).key)).isEqualTo("Edited " + SETTINGS.get(0).key);
        language[0] = "en";
        Map<String, String> values = new LinkedHashMap<>();
        for (Setting s : SETTINGS) {
            values.put(s.path, s.shipped);
        }
        write(values);
        SocialConfig config = load();
        start(config);

        for (Setting s : SETTINGS) {
            assertThat(onDisk().getString(s.path)).as("en, " + s.path + ": the jar's text, not the disk edit").isEqualTo(s.text("en"));
        }

        language[0] = "zh";
        config.init(plugin);
        reload();

        for (Setting s : SETTINGS) {
            assertThat(onDisk().getString(s.path)).as("after a switch to zh, " + s.path + " follows").isEqualTo(s.text("zh"));
        }
    }

    @Test
    @DisplayName("a file that cannot be saved is reported in the server's language naming the file, and the module still uses the new text")
    void saveFailureIsReported() throws Exception {
        language[0] = "en";
        SocialConfig config = spy(load());
        IOException failure = new IOException("read-only");
        doThrow(failure).when(config).save();

        assertThat(start(config)).isTrue();

        String expected = CatalogueText.text("en", "log_config_default_save_failed").replace("{FILE}", "config/social.yml");
        verify(logger).warn(failure, expected);
        for (Setting s : SETTINGS) {
            assertThat(get(config, s)).as(s.field).isEqualTo(s.text("en"));
        }
    }

    @Test
    @DisplayName("@NotEmpty is on exactly the fields that carried it at origin/master, and each text field's Java default is its shipped default")
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    void validationAndJavaDefaults() throws Exception {
        Set<String> notEmpty = new TreeSet<>();
        Set<String> declared = new TreeSet<>();
        for (Field f : SocialConfig.class.getDeclaredFields()) {
            declared.add(f.getName());
            if (f.isAnnotationPresent(ConfigEntry.class) && f.isAnnotationPresent(NotEmpty.class)) {
                notEmpty.add(f.getName());
            }
        }
        assertThat(declared).as("the two master fields left out of the comparison are really gone")
                .doesNotContainAnyElementsOf(DELETED_SINCE_MASTER);
        assertThat(notEmpty).containsExactlyElementsOf(NOT_EMPTY_AT_MASTER);

        SocialConfig fresh = new SocialConfig("config/social.yml");
        for (Setting s : SETTINGS) {
            Field f = SocialConfig.class.getDeclaredField(s.field);
            f.setAccessible(true);
            assertThat(f.get(fresh)).as(s.field).isEqualTo(s.shipped);
            assertThat(f.getAnnotation(ConfigEntry.class).path()).as(s.field).isEqualTo(s.path);
        }
    }

    // ---- consumers: what a player sees is rendered from the file ----

    @Test
    @DisplayName("FriendService's request, accept, deny, block, already-friends, remove and limit messages are rendered from the file's text")
    void friendServiceMessagesUseTheFile() throws Exception {
        for (String code : LANGUAGES) {
            language[0] = code;
            Files.deleteIfExists(file().toPath());
            SocialConfig config = load();
            start(config);
            YamlConfiguration disk = onDisk();
            Players p = new Players();
            Service svc = new Service(config);

            svc.service.sendRequest(p.alice, p.bob);
            svc.service.acceptRequest(p.bob, "Alice");
            svc.service.clearCache(p.aliceUuid);
            svc.service.clearCache(p.bobUuid);
            svc.service.sendRequest(p.bob, p.alice);
            svc.service.denyRequest(p.alice, "Bob");

            assertThat(received(p.alice)).as(code).containsExactly(
                    shown(disk, "messages.request_sent", "Bob"),
                    shown(disk, "messages.friend_added", "Bob"),
                    shown(disk, "messages.request_received", "Bob"),
                    shown(disk, "messages.request_denied", "Bob"));
            assertThat(received(p.bob)).as(code).startsWith(
                    shown(disk, "messages.request_received", "Alice"),
                    shown(disk, "messages.friend_added", "Alice"));

            Players q = new Players();
            Service refusals = new Service(config);
            BlacklistData block = BlacklistData.builder().blockedUuid(q.bobUuid.toString()).blockedName("Bob").build();
            when(refusals.blacklistQuery.list()).thenReturn(new ArrayList<>(Collections.singletonList(block)));
            refusals.service.sendRequest(q.alice, q.bob);
            UltiSocialTestHelper.setField(refusals.service, "blacklistCache", new ConcurrentHashMap<UUID, List<BlacklistData>>());
            when(refusals.blacklistQuery.list()).thenReturn(new ArrayList<BlacklistData>());
            FriendshipData friendship = FriendshipData.builder().friendUuid(q.bobUuid.toString()).friendName("Bob").build();
            when(refusals.friendQuery.list()).thenReturn(new ArrayList<>(Collections.singletonList(friendship)));
            refusals.service.sendRequest(q.alice, q.bob);
            refusals.service.removeFriend(q.alice, "Bob");
            refusals.service.clearCache(q.aliceUuid);
            when(refusals.friendQuery.list()).thenReturn(new ArrayList<FriendshipData>());
            // a limit of 0 friends: the next request is refused at the limit check
            config.setMaxFriends(0);
            refusals.service.sendRequest(q.alice, q.bob);
            config.setMaxFriends(50);

            assertThat(received(q.alice)).as(code).containsExactly(
                    shown(disk, "messages.blocked", "Bob"),
                    shown(disk, "messages.already_friends", "Bob"),
                    shown(disk, "messages.friend_removed", "Bob"),
                    shown(disk, "messages.max_friends_reached", "Bob"));
        }
    }

    @Test
    @DisplayName("SocialListener's online and offline notices are rendered from the file's text")
    void listenerNoticesUseTheFile() throws Exception {
        for (String code : LANGUAGES) {
            language[0] = code;
            Files.deleteIfExists(file().toPath());
            SocialConfig config = load();
            start(config);
            YamlConfiguration disk = onDisk();
            Players p = new Players();
            FriendService listenerService = mock(FriendService.class);
            SocialSeams.speak(listenerService, code);
            when(listenerService.getConfig()).thenReturn(config);
            when(listenerService.areFriends(p.bobUuid, p.aliceUuid)).thenReturn(true);
            doReturn(Arrays.asList(p.alice, p.bob)).when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();
            SocialListener listener = new SocialListener();
            UltiSocialTestHelper.setField(listener, "friendService", listenerService);

            listener.onPlayerJoin(new PlayerJoinEvent(p.alice, (String) null));
            listener.onPlayerQuit(new PlayerQuitEvent(p.alice, (String) null));

            assertThat(received(p.bob)).as(code).containsExactly(
                    shown(disk, "messages.friend_online", "Alice"),
                    shown(disk, "messages.friend_offline", "Alice"));
        }
    }

    @Test
    @DisplayName("FriendListGUI's title is rendered from the file's text")
    void friendListTitleUsesTheFile() throws Exception {
        for (String code : LANGUAGES) {
            language[0] = code;
            Files.deleteIfExists(file().toPath());
            SocialConfig config = load();
            start(config);
            YamlConfiguration disk = onDisk();
            Players p = new Players();
            FriendService guiService = mock(FriendService.class);
            SocialSeams.speak(guiService, code);
            when(guiService.getConfig()).thenReturn(config);
            when(guiService.getFriends(p.aliceUuid)).thenReturn(new ArrayList<FriendshipData>());
            when(guiService.getPendingRequests(p.aliceUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().createInventory(any(), anyInt(), anyString()))
                    .thenReturn(mock(Inventory.class));

            try (MockedConstruction<ItemStack> items = mockConstruction(ItemStack.class,
                    (item, context) -> lenient().when(item.getItemMeta()).thenReturn(mock(ItemMeta.class)))) {
                new FriendListGUI(guiService, p.alice);
            }

            ArgumentCaptor<String> title = ArgumentCaptor.forClass(String.class);
            verify(UltiSocialTestHelper.getMockServer(), atLeastOnce()).createInventory(any(), eq(54), title.capture());
            assertThat(title.getValue()).as(code).isEqualTo(disk.getString("gui_title")
                    .replace("{COUNT}", "0").replace("{MAX}", "50").replace("&", "§"));
        }
    }

    /** How the readers render a message: placeholders, then every {@code &} becomes {@code §}. */
    private static String shown(YamlConfiguration disk, String path, String player) {
        String value = disk.getString(path);
        assertThat(value).as("control: " + path + " holds text").isNotNull().isNotEmpty();
        return value.replace("{PLAYER}", player).replace("&", "§");
    }

    private static List<String> received(Player player) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues();
    }

    /** Two online players, Alice and Bob, each with a fresh id. */
    private static final class Players {
        final UUID aliceUuid = UUID.randomUUID();
        final UUID bobUuid = UUID.randomUUID();
        final Player alice = UltiSocialTestHelper.createMockPlayer("Alice", aliceUuid);
        final Player bob = UltiSocialTestHelper.createMockPlayer("Bob", bobUuid);

        Players() {
            lenient().when(UltiSocialTestHelper.getMockServer().getPlayer(aliceUuid)).thenReturn(alice);
            lenient().when(UltiSocialTestHelper.getMockServer().getPlayer(bobUuid)).thenReturn(bob);
        }
    }

    /** A real {@link FriendService} over {@code config}, with empty friend and blacklist tables. */
    private final class Service {
        final FriendService service = new FriendService();
        @SuppressWarnings("unchecked")
        final DataOperator<FriendshipData> friends = mock(DataOperator.class);
        @SuppressWarnings("unchecked")
        final DataOperator<BlacklistData> blacklist = mock(DataOperator.class);
        @SuppressWarnings("unchecked")
        final Query<FriendshipData> friendQuery = mock(Query.class);
        @SuppressWarnings("unchecked")
        final Query<BlacklistData> blacklistQuery = mock(Query.class);

        Service(SocialConfig config) throws Exception {
            UltiToolsPlugin module = mock(UltiToolsPlugin.class);
            lenient().when(module.i18n(anyString())).thenAnswer(CatalogueText.answer(language[0]));
            lenient().when(module.getLogger()).thenReturn(logger);
            lenient().when(friendQuery.where(anyString())).thenReturn(friendQuery);
            lenient().when(friendQuery.and(anyString())).thenReturn(friendQuery);
            lenient().when(friendQuery.eq(any())).thenReturn(friendQuery);
            lenient().when(blacklistQuery.where(anyString())).thenReturn(blacklistQuery);
            lenient().when(blacklistQuery.and(anyString())).thenReturn(blacklistQuery);
            lenient().when(blacklistQuery.eq(any())).thenReturn(blacklistQuery);
            lenient().when(friends.query()).thenReturn(friendQuery);
            lenient().when(blacklist.query()).thenReturn(blacklistQuery);
            lenient().when(friendQuery.list()).thenReturn(new ArrayList<FriendshipData>());
            lenient().when(blacklistQuery.list()).thenReturn(new ArrayList<BlacklistData>());
            UltiSocialTestHelper.setField(service, "plugin", module);
            UltiSocialTestHelper.setField(service, "config", config);
            UltiSocialTestHelper.setField(service, "dataOperator", friends);
            UltiSocialTestHelper.setField(service, "blacklistDataOperator", blacklist);
        }
    }

    // ---- harness ----

    private static String get(SocialConfig config, Setting s) throws Exception {
        return (String) SocialConfig.class.getMethod(s.getter()).invoke(config);
    }

    private File file() {
        return new File(tempDir.toFile(), "config/social.yml");
    }

    private byte[] bytes() throws IOException {
        return Files.readAllBytes(file().toPath());
    }

    private YamlConfiguration onDisk() {
        return YamlConfiguration.loadConfiguration(file());
    }

    /** Writes a social.yml holding {@code values} (path to value), as an earlier version or an operator left it. */
    private void write(Map<String, String> values) throws IOException {
        Files.createDirectories(file().getParentFile().toPath());
        YamlConfiguration persisted = new YamlConfiguration();
        for (Map.Entry<String, String> e : values.entrySet()) {
            persisted.set(e.getKey(), e.getValue());
        }
        persisted.save(file());
    }

    /** The framework's own load: {@code init} fills missing keys with the Java defaults, saves, validates. */
    private SocialConfig load() throws IOException {
        Files.createDirectories(file().getParentFile().toPath());
        SocialConfig config = new SocialConfig("config/social.yml");
        config.init(plugin);
        return config;
    }

    /** The module's enable path: {@code UltiSocial#registerSelf()} with {@code config} as the module's configuration. */
    private boolean start(SocialConfig config) {
        current = config;
        return plugin.registerSelf();
    }

    /** The module's {@code onReload()} (protected), as the framework calls it after rebuilding the language. */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private void reload() throws Exception {
        java.lang.reflect.Method onReload = UltiSocial.class.getDeclaredMethod("onReload");
        onReload.setAccessible(true);
        onReload.invoke(plugin);
    }

    /**
     * A module double whose {@code registerSelf()} and {@code onReload()} are the real ones, whose
     * configuration folder is the temporary directory, whose {@code i18n} answers from the module's real
     * catalogue for the language in {@link #language} (read at call time) unless {@link #diskOverrides}
     * holds an operator's edit, and whose {@code getConfig(SocialConfig.class)} is {@link #current}.
     */
    private UltiSocial moduleDouble() {
        return Mockito.mock(UltiSocial.class, this::moduleAnswer);
    }

    private Object moduleAnswer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
        String name = invocation.getMethod().getName();
        switch (name) {
            case "registerSelf":
            case "onReload":
            case "operatorConfigPath":
                return invocation.callRealMethod();
            case "getConfigFolder":
                return tempDir.toString();
            case "getConfigFile":
                return new File(tempDir.toFile(), invocation.<String>getArgument(0));
            case "operatorConfigFile":
                return file();
            case "i18n": {
                String key = invocation.getArgument(invocation.getArguments().length - 1);
                return diskOverrides.containsKey(key) ? diskOverrides.get(key)
                        : CatalogueText.answer(language[0]).answer(invocation);
            }
            case "getLanguageCode":
                return language[0];
            case "getLogger":
                return logger;
            case "getConfig":
                return invocation.getArguments().length == 1 && invocation.getArgument(0) == SocialConfig.class
                        ? current : Answers.RETURNS_DEFAULTS.answer(invocation);
            default:
                return Answers.RETURNS_DEFAULTS.answer(invocation);
        }
    }
}
