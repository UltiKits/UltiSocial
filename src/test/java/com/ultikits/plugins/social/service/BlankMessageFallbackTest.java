package com.ultikits.plugins.social.service;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.gui.FriendListGUI;
import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.plugins.social.i18n.SocialSeams;
import com.ultikits.plugins.social.listener.SocialListener;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.interfaces.DataOperator;
import com.ultikits.ultitools.interfaces.Query;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A blank message or title in {@code config/social.yml} shows the language file's text in the server's
 * language; any other value is the operator's and is shown as written (maintainer ruling 2026-09-24 (d),
 * UltiKits/UltiSocial#14).
 * <p>
 * Each of the eleven settings is driven through the path that really shows it, once per language, with
 * every setting blank. Before the change a blank value was sent as an empty line.
 */
@DisplayName("A blank social message shows the language file's text")
class BlankMessageFallbackTest {

    private FriendService service;
    private SocialConfig config;
    private UltiToolsPlugin plugin;
    @SuppressWarnings("unchecked")
    private final DataOperator<FriendshipData> friends = mock(DataOperator.class);
    @SuppressWarnings("unchecked")
    private final DataOperator<BlacklistData> blacklist = mock(DataOperator.class);
    @SuppressWarnings("unchecked")
    private final Query<FriendshipData> friendQuery = mock(Query.class);
    @SuppressWarnings("unchecked")
    private final Query<BlacklistData> blacklistQuery = mock(Query.class);
    private Player alice;
    private Player bob;
    private UUID aliceUuid;
    private UUID bobUuid;

    private void build(String language) throws Exception {
        config = mock(SocialConfig.class);
        lenient().when(config.getGuiTitle()).thenReturn("");
        lenient().when(config.getFriendAddedMessage()).thenReturn("");
        lenient().when(config.getFriendRemovedMessage()).thenReturn("");
        lenient().when(config.getFriendOnlineMessage()).thenReturn("");
        lenient().when(config.getFriendOfflineMessage()).thenReturn(" ");
        lenient().when(config.getRequestSentMessage()).thenReturn("");
        lenient().when(config.getRequestReceivedMessage()).thenReturn("");
        lenient().when(config.getRequestDeniedMessage()).thenReturn("");
        lenient().when(config.getMaxFriendsMessage()).thenReturn("");
        lenient().when(config.getAlreadyFriendsMessage()).thenReturn("");
        lenient().when(config.getBlockedMessage()).thenReturn("");
        lenient().when(config.getMaxFriends()).thenReturn(50);
        lenient().when(config.getRequestTimeout()).thenReturn(60);
        lenient().when(config.isNotifyFriendOnline()).thenReturn(true);
        lenient().when(config.isNotifyFriendOffline()).thenReturn(true);
        plugin = mock(UltiToolsPlugin.class);
        when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer(language));
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
        service = new FriendService();
        UltiSocialTestHelper.setField(service, "plugin", plugin);
        UltiSocialTestHelper.setField(service, "config", config);
        UltiSocialTestHelper.setField(service, "dataOperator", friends);
        UltiSocialTestHelper.setField(service, "blacklistDataOperator", blacklist);
        aliceUuid = UUID.randomUUID();
        bobUuid = UUID.randomUUID();
        alice = UltiSocialTestHelper.createMockPlayer("Alice", aliceUuid);
        bob = UltiSocialTestHelper.createMockPlayer("Bob", bobUuid);
        lenient().when(UltiSocialTestHelper.getMockServer().getPlayer(aliceUuid)).thenReturn(alice);
        lenient().when(UltiSocialTestHelper.getMockServer().getPlayer(bobUuid)).thenReturn(bob);
    }

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    private static String text(String language, String key, String... tokenValuePairs) {
        String value = CatalogueText.entries(language).get(key);
        if (value == null) {
            return "<lang/" + language + " has no " + key + ">";
        }
        for (int i = 0; i + 1 < tokenValuePairs.length; i += 2) {
            value = value.replace(tokenValuePairs[i], tokenValuePairs[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    private static List<String> received(Player p) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(p, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues();
    }

    @ParameterizedTest(name = "language: {0}")
    @ValueSource(strings = {"en", "zh"})
    @DisplayName("request sent and received, accepted, denied")
    void requests(String language) throws Exception {
        build(language);

        service.sendRequest(alice, bob);
        service.acceptRequest(bob, "Alice");
        service.clearCache(aliceUuid);
        service.clearCache(bobUuid);
        service.sendRequest(bob, alice);
        service.denyRequest(alice, "Bob");

        assertThat(received(alice)).containsExactly(
                text(language, "request_sent", "{PLAYER}", "Bob"),
                text(language, "friend_added", "{PLAYER}", "Bob"),
                text(language, "request_received", "{PLAYER}", "Bob"),
                text(language, "request_denied", "{PLAYER}", "Bob"));
        assertThat(received(bob)).startsWith(
                text(language, "request_received", "{PLAYER}", "Alice"),
                text(language, "friend_added", "{PLAYER}", "Alice"));
    }

    @ParameterizedTest(name = "language: {0}")
    @ValueSource(strings = {"en", "zh"})
    @DisplayName("blocked, already friends, friend removed, friend limit reached")
    void refusalsAndRemoval(String language) throws Exception {
        build(language);
        BlacklistData block = BlacklistData.builder().blockedUuid(bobUuid.toString()).blockedName("Bob").build();
        when(blacklistQuery.list()).thenReturn(new ArrayList<>(Collections.singletonList(block)));
        service.sendRequest(alice, bob);
        UltiSocialTestHelper.setField(service, "blacklistCache", new ConcurrentHashMap<UUID, List<BlacklistData>>());
        when(blacklistQuery.list()).thenReturn(new ArrayList<BlacklistData>());
        FriendshipData friendship = FriendshipData.builder().friendUuid(bobUuid.toString()).friendName("Bob").build();
        when(friendQuery.list()).thenReturn(new ArrayList<>(Collections.singletonList(friendship)));
        service.sendRequest(alice, bob);
        service.removeFriend(alice, "Bob");
        service.clearCache(aliceUuid);
        when(friendQuery.list()).thenReturn(new ArrayList<FriendshipData>());
        when(config.getMaxFriends()).thenReturn(0);
        service.sendRequest(alice, bob);

        assertThat(received(alice)).containsExactly(
                text(language, "blocked", "{PLAYER}", "Bob"),
                text(language, "already_friends", "{PLAYER}", "Bob"),
                text(language, "friend_removed", "{PLAYER}", "Bob"),
                text(language, "max_friends_reached"));
    }

    @ParameterizedTest(name = "language: {0}")
    @ValueSource(strings = {"en", "zh"})
    @DisplayName("a friend coming online and going offline (a whitespace-only value counts as blank)")
    void onlineOffline(String language) throws Exception {
        build(language);
        FriendService listenerService = mock(FriendService.class);
        SocialSeams.speak(listenerService, language);
        when(listenerService.getConfig()).thenReturn(config);
        when(listenerService.areFriends(bobUuid, aliceUuid)).thenReturn(true);
        doReturn(Arrays.asList(alice, bob)).when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();
        SocialListener listener = new SocialListener();
        UltiSocialTestHelper.setField(listener, "friendService", listenerService);

        listener.onPlayerJoin(new PlayerJoinEvent(alice, (String) null));
        listener.onPlayerQuit(new PlayerQuitEvent(alice, (String) null));

        assertThat(received(bob)).containsExactly(
                text(language, "friend_online", "{PLAYER}", "Alice"),
                text(language, "friend_offline", "{PLAYER}", "Alice"));
    }

    @ParameterizedTest(name = "language: {0}")
    @ValueSource(strings = {"en", "zh"})
    @DisplayName("the friend list title")
    void guiTitle(String language) throws Exception {
        build(language);
        FriendService guiService = mock(FriendService.class);
        SocialSeams.speak(guiService, language);
        when(guiService.getConfig()).thenReturn(config);
        when(guiService.getFriends(aliceUuid)).thenReturn(new ArrayList<FriendshipData>());
        when(guiService.getPendingRequests(aliceUuid)).thenReturn(new ArrayList<>());
        when(UltiSocialTestHelper.getMockServer().createInventory(any(), anyInt(), anyString()))
                .thenReturn(mock(Inventory.class));

        try (MockedConstruction<ItemStack> items = mockConstruction(ItemStack.class,
                (item, context) -> lenient().when(item.getItemMeta()).thenReturn(mock(ItemMeta.class)))) {
            new FriendListGUI(guiService, alice);
        }

        ArgumentCaptor<String> title = ArgumentCaptor.forClass(String.class);
        verify(UltiSocialTestHelper.getMockServer()).createInventory(any(), eq(54), title.capture());
        assertThat(title.getValue()).isEqualTo(text(language, "gui_friend_list", "{COUNT}", "0", "{MAX}", "50"));
    }
}
