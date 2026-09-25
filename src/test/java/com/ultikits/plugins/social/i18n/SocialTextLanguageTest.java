package com.ultikits.plugins.social.i18n;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.commands.FriendCommand;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendRequest;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.gui.BlockListGUI;
import com.ultikits.plugins.social.gui.FriendListGUI;
import com.ultikits.plugins.social.listener.SocialListener;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.annotations.command.CmdExecutor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Text this module shows to players and writes to the console follows the framework's
 * {@code language} setting (UltiKits/UltiSocial#14).
 * <p>
 * The service's {@code i18n} answers from the catalogue this module really ships ({@link CatalogueText}).
 * Before the language sweep {@code /friend}, both GUIs and the GUI click replies were fixed Chinese
 * text, although the catalogue already held English and Chinese text for most of them that no code read.
 */
@DisplayName("UltiSocial text follows the language setting (UltiKits/UltiSocial#14)")
class SocialTextLanguageTest {

    private static final Pattern CJK = Pattern.compile("[\\u4e00-\\u9fff]");

    private FriendService service;
    private SocialConfig config;
    private Player player;
    private UUID playerUuid;

    /** The English catalogue text for {@code key}, colour codes applied, tokens replaced. */
    static String en(String key, String... tokenValuePairs) {
        String value = CatalogueText.entries("en").get(key);
        if (value == null) {
            return "<lang/en has no " + key + ">";
        }
        for (int i = 0; i + 1 < tokenValuePairs.length; i += 2) {
            value = value.replace(tokenValuePairs[i], tokenValuePairs[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
        service = mock(FriendService.class);
        SocialSeams.speak(service, "en");
        config = UltiSocialTestHelper.createDefaultConfig();
        lenient().when(service.getConfig()).thenReturn(config);
        playerUuid = UUID.randomUUID();
        player = UltiSocialTestHelper.createMockPlayer("Alice", playerUuid);
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    private List<String> sentTo(Player p) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(p, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues();
    }

    private static FriendshipData friend(UUID uuid, String name) {
        return FriendshipData.builder().friendUuid(uuid.toString()).friendName(name)
                .createdTime(0L).build();
    }

    @Nested
    @DisplayName("/friend under language: en")
    class Command {

        private FriendCommand command;

        @BeforeEach
        void build() {
            command = new FriendCommand(service, null);
        }

        @Test
        @DisplayName("the command description the framework translates has English text")
        void description() {
            String key = FriendCommand.class.getAnnotation(CmdExecutor.class).description();

            assertThat(CatalogueText.entries("en").get(key)).isEqualTo("Friend system");
        }

        @Test
        @DisplayName("an empty friend list, and a list with one friend online")
        void list() {
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<FriendshipData>());
            command.listFriends(player);

            UUID bob = UUID.randomUUID();
            FriendshipData data = friend(bob, "Bob");
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(data)));
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(mock(Player.class));
                command.listFriends(player);
            }

            assertThat(sentTo(player)).containsExactly(
                    en("list_empty"),
                    en("list_header", "{COUNT}", "1"),
                    en("status_online") + " " + ChatColor.WHITE + "Bob");
        }

        @Test
        @DisplayName("adding a player who is offline, and adding oneself")
        void add() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayerExact("Ghost")).thenReturn(null);
                bukkit.when(() -> Bukkit.getPlayerExact("Alice")).thenReturn(player);
                command.addFriend(player, "Ghost");
                command.addFriend(player, "Alice");
            }

            assertThat(sentTo(player)).containsExactly(
                    en("player_not_online", "{PLAYER}", "Ghost"),
                    en("cannot_add_self"));
        }

        @Test
        @DisplayName("a name the player typed is echoed as typed, never colour-translated (gate-1 IN-01)")
        void typedNameIsNotColourTranslated() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayerExact("&kabc")).thenReturn(null);
                command.addFriend(player, "&kabc");
            }

            String template = CatalogueText.entries("en").get("player_not_online");
            assertThat(sentTo(player)).containsExactly(template == null ? "<lang/en has no player_not_online>"
                    : ChatColor.translateAlternateColorCodes('&', template).replace("{PLAYER}", "&kabc"));
        }

        @Test
        @DisplayName("no pending requests, then one pending request")
        void requests() {
            when(service.getPendingRequests(playerUuid)).thenReturn(new ArrayList<FriendRequest>());
            command.viewRequests(player);
            FriendRequest request = mock(FriendRequest.class);
            when(request.getSenderName()).thenReturn("Bob");
            when(service.getPendingRequests(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(request)));
            command.viewRequests(player);

            assertThat(sentTo(player)).containsExactly(
                    en("requests_empty"),
                    en("requests_header"),
                    en("requests_entry", "{PLAYER}", "Bob"));
        }

        @Test
        @DisplayName("teleporting: disabled, not a friend, offline, cooling down, success")
        void teleport() {
            UUID bob = UUID.randomUUID();
            when(config.isTpToFriendEnabled()).thenReturn(false);
            command.teleportToFriend(player, "Bob");
            when(config.isTpToFriendEnabled()).thenReturn(true);
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<FriendshipData>());
            command.teleportToFriend(player, "Bob");
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(friend(bob, "Bob"))));
            Player online = mock(Player.class);
            when(online.getLocation()).thenReturn(mock(Location.class));
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(null);
                command.teleportToFriend(player, "Bob");
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(online);
                when(service.canTeleport(playerUuid)).thenReturn(false);
                when(service.getRemainingCooldown(playerUuid)).thenReturn(12);
                command.teleportToFriend(player, "Bob");
                when(service.canTeleport(playerUuid)).thenReturn(true);
                command.teleportToFriend(player, "Bob");
            }

            assertThat(sentTo(player)).containsExactly(
                    en("tp_disabled"),
                    en("not_friend", "{PLAYER}", "Bob"),
                    en("friend_not_online", "{PLAYER}", "Bob"),
                    en("tp_cooldown", "{SECONDS}", "12"),
                    en("tp_success", "{PLAYER}", "Bob"));
        }

        @Test
        @DisplayName("private messages: not a friend, empty message, and the two lines of a sent message")
        void privateMessage() {
            UUID bob = UUID.randomUUID();
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<FriendshipData>());
            command.sendMessage(player, "Bob", new String[] {"hi"});
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(friend(bob, "Bob"))));
            Player target = mock(Player.class);
            when(target.getName()).thenReturn("Bob");
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(target);
                command.sendMessage(player, "Bob", new String[] {" "});
                command.sendMessage(player, "Bob", new String[] {"hello", "there"});
            }

            assertThat(sentTo(player)).containsExactly(
                    en("msg_only_friend", "{PLAYER}", "Bob"),
                    en("msg_empty"),
                    en("msg_prefix") + " " + en("msg_sent", "{RECEIVER}", "Bob") + "hello there");
            assertThat(sentTo(target)).containsExactly(
                    en("msg_prefix") + " " + en("msg_received", "{SENDER}", "Alice") + "hello there");
        }

        @Test
        @DisplayName("blocking and unblocking")
        void blocking() {
            Player bobPlayer = mock(Player.class);
            UUID bob = UUID.randomUUID();
            when(bobPlayer.getUniqueId()).thenReturn(bob);
            OfflinePlayer never = mock(OfflinePlayer.class);
            when(never.hasPlayedBefore()).thenReturn(false);
            when(service.addToBlacklist(player, bobPlayer, null)).thenReturn(true, false);
            when(service.areFriends(playerUuid, bob)).thenReturn(true);
            when(service.removeFromBlacklist(player, "Bob")).thenReturn(true, false);
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayerExact("Nobody")).thenReturn(null);
                bukkit.when(() -> Bukkit.getOfflinePlayer("Nobody")).thenReturn(never);
                bukkit.when(() -> Bukkit.getPlayerExact("Bob")).thenReturn(bobPlayer);
                bukkit.when(() -> Bukkit.getPlayerExact("Alice")).thenReturn(player);
                command.blockPlayer(player, "Nobody");
                command.blockPlayer(player, "Alice");
                command.blockPlayer(player, "Bob");
                command.blockPlayer(player, "Bob");
            }
            command.unblockPlayer(player, "Bob");
            command.unblockPlayer(player, "Bob");

            assertThat(sentTo(player)).containsExactly(
                    en("player_not_exist", "{PLAYER}", "Nobody"),
                    en("cannot_block_self"),
                    en("player_blocked", "{PLAYER}", "Bob"),
                    en("auto_unfriend"),
                    en("already_blocked", "{PLAYER}", "Bob"),
                    en("player_unblocked", "{PLAYER}", "Bob"),
                    en("not_in_blocklist", "{PLAYER}", "Bob"));
        }

        @Test
        @DisplayName("the help lines")
        void help() {
            command.help(player);

            List<String> keys = Arrays.asList("help_title", "help_friend", "help_list", "help_add", "help_accept",
                    "help_deny", "help_remove", "help_tp", "help_msg", "help_requests", "help_block_title",
                    "help_block", "help_unblock", "help_blocklist");
            List<String> expected = new ArrayList<>();
            for (String key : keys) {
                expected.add(en(key));
            }
            assertThat(sentTo(player)).containsExactlyElementsOf(expected);
            assertThat(sentTo(player)).noneMatch(l -> CJK.matcher(l).find());
        }
    }

    @Nested
    @DisplayName("GUIs under language: en")
    class Guis {

        private Inventory inventory;
        private final List<String> names = new ArrayList<>();
        private final List<String> lore = new ArrayList<>();

        @BeforeEach
        void inventory() {
            inventory = mock(Inventory.class);
            when(UltiSocialTestHelper.getMockServer().createInventory(any(), anyInt(), anyString())).thenReturn(inventory);
        }

        @SuppressWarnings("unchecked")
        private MockedConstruction<ItemStack> recordingItems() {
            return mockConstruction(ItemStack.class, (item, context) -> {
                SkullMeta meta = mock(SkullMeta.class);
                lenient().doAnswer(inv -> names.add(inv.getArgument(0))).when(meta).setDisplayName(anyString());
                lenient().doAnswer(inv -> lore.addAll((List<String>) inv.getArgument(0))).when(meta).setLore(any());
                lenient().when(item.getItemMeta()).thenReturn(meta);
            });
        }

        private String title() {
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(UltiSocialTestHelper.getMockServer()).createInventory(any(), eq(54), captor.capture());
            return captor.getValue();
        }

        @Test
        @DisplayName("the friend list: an online friend's lines and the navigation row")
        void friendList() {
            UUID bob = UUID.randomUUID();
            FriendshipData data = friend(bob, "Bob");
            when(service.getFriends(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(data)));
            FriendRequest request = mock(FriendRequest.class);
            when(service.getPendingRequests(playerUuid)).thenReturn(new ArrayList<>(Arrays.asList(request, request)));
            Player online = mock(Player.class);
            World world = mock(World.class);
            when(world.getName()).thenReturn("world");
            when(online.getWorld()).thenReturn(world);
            when(online.getGameMode()).thenReturn(GameMode.SURVIVAL);
            when(UltiSocialTestHelper.getMockServer().getPlayer(bob)).thenReturn(online);
            when(UltiSocialTestHelper.getMockServer().getOfflinePlayer(bob)).thenReturn(mock(OfflinePlayer.class));

            try (MockedConstruction<ItemStack> items = recordingItems()) {
                new FriendListGUI(service, player);
            }

            assertThat(lore).startsWith(
                    en("status_online"),
                    en("gui_world", "{WORLD}", "world"),
                    en("gui_mode", "{MODE}", "Survival"),
                    en("gui_added_time", "{TIME}", new SimpleDateFormat("yyyy-MM-dd").format(new Date(0L))),
                    "",
                    en("gui_click_tp"),
                    en("gui_click_msg"),
                    en("gui_click_favorite"),
                    en("gui_click_remove"));
            assertThat(names).contains(en("gui_requests", "{COUNT}", "2"), en("gui_page", "{PAGE}", "1", "{TOTAL}", "1"));
            assertThat(lore).contains(en("gui_click_view"));
            assertThat(names).noneMatch(l -> CJK.matcher(l).find());
            assertThat(lore).noneMatch(l -> CJK.matcher(l).find());
        }

        @Test
        @DisplayName("the blacklist: its title, a blocked player's lines and the navigation row")
        void blockList() {
            BlacklistData blocked = mock(BlacklistData.class);
            UUID bob = UUID.randomUUID();
            when(blocked.getBlockedUuid()).thenReturn(bob.toString());
            when(UltiSocialTestHelper.getMockServer().getOfflinePlayer(bob)).thenReturn(mock(OfflinePlayer.class));
            when(blocked.getBlockedName()).thenReturn("Bob");
            when(blocked.getReason()).thenReturn("spam");
            when(blocked.getCreatedTime()).thenReturn(0L);
            when(service.getBlacklist(playerUuid)).thenReturn(new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> items = recordingItems()) {
                new BlockListGUI(service, player);
            }

            assertThat(title()).isEqualTo(en("gui_blocklist") + " " + ChatColor.GRAY + "(1)");
            assertThat(lore).contains(
                    en("gui_blocked_time", "{TIME}", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(0L))),
                    en("gui_block_reason", "{REASON}", "spam"),
                    en("gui_click_unblock"),
                    en("gui_unblock_hint"),
                    en("gui_click_back"),
                    en("gui_blocked_total", "{COUNT}", "1"));
            assertThat(names).contains(en("gui_back"), en("gui_page", "{PAGE}", "1", "{TOTAL}", "1"));
            assertThat(names).noneMatch(l -> CJK.matcher(l).find());
            assertThat(lore).noneMatch(l -> CJK.matcher(l).find());
        }

        @Test
        @DisplayName("an empty blacklist")
        void emptyBlockList() {
            when(service.getBlacklist(playerUuid)).thenReturn(new ArrayList<BlacklistData>());

            try (MockedConstruction<ItemStack> items = recordingItems()) {
                new BlockListGUI(service, player);
            }

            assertThat(names).contains(en("gui_blocklist_empty"));
            assertThat(lore).contains(en("gui_blocklist_empty_hint"));
        }
    }

    @Nested
    @DisplayName("GUI click replies under language: en")
    class ClickReplies {

        private SocialListener listener;

        @BeforeEach
        void build() throws Exception {
            listener = new SocialListener();
            UltiSocialTestHelper.setField(listener, "friendService", service);
        }

        private InventoryClickEvent click(Object holder, int slot, ClickType type) {
            Inventory inventory = mock(Inventory.class);
            when(inventory.getHolder()).thenReturn((org.bukkit.inventory.InventoryHolder) holder);
            InventoryClickEvent event = mock(InventoryClickEvent.class);
            lenient().when(event.getInventory()).thenReturn(inventory);
            lenient().when(event.getRawSlot()).thenReturn(slot);
            lenient().when(event.getWhoClicked()).thenReturn(player);
            lenient().when(event.isLeftClick()).thenReturn(type == ClickType.LEFT || type == ClickType.SHIFT_LEFT);
            lenient().when(event.isRightClick()).thenReturn(type == ClickType.RIGHT || type == ClickType.SHIFT_RIGHT);
            lenient().when(event.isShiftClick()).thenReturn(type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT);
            return event;
        }

        @Test
        @DisplayName("friend list clicks: favourite, cooldown, teleport, offline, private-message hint")
        void friendListClicks() {
            UUID bob = UUID.randomUUID();
            FriendListGUI gui = mock(FriendListGUI.class);
            when(gui.getFriendAtSlot(5)).thenReturn(friend(bob, "Bob"));
            Player online = mock(Player.class);
            when(online.getLocation()).thenReturn(mock(Location.class));
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(online);
                listener.onInventoryClick(click(gui, 5, ClickType.SHIFT_LEFT));
                when(service.canTeleport(playerUuid)).thenReturn(false);
                when(service.getRemainingCooldown(playerUuid)).thenReturn(7);
                listener.onInventoryClick(click(gui, 5, ClickType.LEFT));
                when(service.canTeleport(playerUuid)).thenReturn(true);
                listener.onInventoryClick(click(gui, 5, ClickType.LEFT));
                listener.onInventoryClick(click(gui, 5, ClickType.RIGHT));
                bukkit.when(() -> Bukkit.getPlayer(bob)).thenReturn(null);
                listener.onInventoryClick(click(gui, 5, ClickType.LEFT));
            }

            assertThat(sentTo(player)).containsExactly(
                    en("favorite_updated"),
                    en("tp_cooldown", "{SECONDS}", "7"),
                    en("tp_success", "{PLAYER}", "Bob"),
                    en("msg_use_command", "{PLAYER}", "Bob"),
                    en("friend_not_online", "{PLAYER}", "Bob"));
        }

        @Test
        @DisplayName("blacklist clicks: unblocked, and failed to unblock")
        void blockListClicks() {
            BlockListGUI gui = mock(BlockListGUI.class);
            BlacklistData blocked = mock(BlacklistData.class);
            when(blocked.getBlockedName()).thenReturn("Bob");
            when(gui.getBlockedUserAtSlot(3)).thenReturn(blocked);
            when(service.removeFromBlacklist(player, "Bob")).thenReturn(true, false);

            listener.onInventoryClick(click(gui, 3, ClickType.LEFT));
            listener.onInventoryClick(click(gui, 3, ClickType.LEFT));

            assertThat(sentTo(player)).containsExactly(
                    en("player_unblocked", "{PLAYER}", "Bob"),
                    en("unblock_failed"));
        }
    }
}
