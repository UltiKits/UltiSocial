package com.ultikits.plugins.social.commands;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendRequest;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.services.TeleportService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FriendCommand.
 */
@DisplayName("FriendCommand Tests")
class FriendCommandTest {

    private FriendCommand command;
    private FriendService friendService;
    private TeleportService teleportService;
    private SocialConfig config;

    private Player player;
    private Player target;
    private UUID playerUuid;
    private UUID targetUuid;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();

        friendService = mock(FriendService.class);
        teleportService = mock(TeleportService.class);
        config = UltiSocialTestHelper.createDefaultConfig();

        // Mock config getter for all tests
        lenient().when(friendService.getConfig()).thenReturn(config);

        command = new FriendCommand(friendService, teleportService);

        playerUuid = UUID.randomUUID();
        targetUuid = UUID.randomUUID();
        player = UltiSocialTestHelper.createMockPlayer("TestPlayer", playerUuid);
        target = UltiSocialTestHelper.createMockPlayer("TargetPlayer", targetUuid);
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    // ==================== listFriends ====================

    @Nested
    @DisplayName("listFriends")
    class ListFriends {

        @Test
        @DisplayName("Should list friends when player has friends")
        void listFriendsWithFriends() {
            UUID friend1Uuid = UUID.randomUUID();
            UUID friend2Uuid = UUID.randomUUID();

            FriendshipData friend1 = FriendshipData.builder()
                    .friendUuid(friend1Uuid.toString())
                    .friendName("Friend1")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData friend2 = FriendshipData.builder()
                    .friendUuid(friend2Uuid.toString())
                    .friendName("Friend2")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(playerUuid))
                    .thenReturn(Arrays.asList(friend1, friend2));

            // Mock Bukkit.getPlayer() to return null (offline friends)
            when(UltiSocialTestHelper.getMockServer().getPlayer(friend1Uuid)).thenReturn(null);
            when(UltiSocialTestHelper.getMockServer().getPlayer(friend2Uuid)).thenReturn(null);

            command.listFriends(player);

            verify(player, atLeastOnce()).sendMessage(anyString());
        }

        @Test
        @DisplayName("Should show message when no friends")
        void listFriendsNoFriends() {
            when(friendService.getFriends(playerUuid))
                    .thenReturn(new ArrayList<>());

            command.listFriends(player);

            verify(player).sendMessage(contains("还没有好友"));
        }

        @Test
        @DisplayName("Should show online status for online friends")
        void listFriendsOnlineStatus() {
            UUID friend1Uuid = UUID.randomUUID();

            FriendshipData friend1 = FriendshipData.builder()
                    .friendUuid(friend1Uuid.toString())
                    .friendName("OnlineFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend1));

            // Friend is online
            Player onlineFriend = UltiSocialTestHelper.createMockPlayer("OnlineFriend", friend1Uuid);
            when(UltiSocialTestHelper.getMockServer().getPlayer(friend1Uuid)).thenReturn(onlineFriend);

            command.listFriends(player);

            // Should show header and friend with online status
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            boolean hasOnlineIndicator = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("在线"));
            assertThat(hasOnlineIndicator).isTrue();
        }

        @Test
        @DisplayName("Should show star for favorite friends")
        void listFriendsShowStar() {
            UUID friend1Uuid = UUID.randomUUID();

            FriendshipData friend1 = FriendshipData.builder()
                    .friendUuid(friend1Uuid.toString())
                    .friendName("FavFriend")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend1));
            when(UltiSocialTestHelper.getMockServer().getPlayer(friend1Uuid)).thenReturn(null);

            command.listFriends(player);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            // Should contain star character for favorite
            boolean hasStar = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("\u2605")); // Star symbol
            assertThat(hasStar).isTrue();
        }

        @Test
        @DisplayName("Should show friend count in header")
        void listFriendsShowCount() {
            FriendshipData friend1 = FriendshipData.builder()
                    .friendUuid(UUID.randomUUID().toString())
                    .friendName("Friend1")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend1));
            when(UltiSocialTestHelper.getMockServer().getPlayer(any(UUID.class))).thenReturn(null);

            command.listFriends(player);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            boolean hasCount = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("(1)"));
            assertThat(hasCount).isTrue();
        }
    }

    // ==================== addFriend ====================

    @Nested
    @DisplayName("addFriend")
    class AddFriend {

        @Test
        @DisplayName("Should send friend request to online player")
        void addFriendOnline() {
            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TargetPlayer"))
                        .thenReturn(target);

                command.addFriend(player, "TargetPlayer");

                verify(friendService).sendRequest(player, target);
            }
        }

        @Test
        @DisplayName("Should show error when player offline")
        void addFriendOffline() {
            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("OfflinePlayer"))
                        .thenReturn(null);

                command.addFriend(player, "OfflinePlayer");

                verify(player).sendMessage(contains("不在线"));
            }
        }

        @Test
        @DisplayName("Should show error when trying to add self")
        void addFriendSelf() {
            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TestPlayer"))
                        .thenReturn(player);

                command.addFriend(player, "TestPlayer");

                verify(player).sendMessage(contains("不能添加自己"));
            }
        }
    }

    // ==================== acceptRequest ====================

    @Nested
    @DisplayName("acceptRequest")
    class AcceptRequest {

        @Test
        @DisplayName("Should accept friend request")
        void acceptRequest() {
            command.acceptRequest(player, "Sender");

            verify(friendService).acceptRequest(player, "Sender");
        }
    }

    // ==================== denyRequest ====================

    @Nested
    @DisplayName("denyRequest")
    class DenyRequest {

        @Test
        @DisplayName("Should deny friend request")
        void denyRequest() {
            command.denyRequest(player, "Sender");

            verify(friendService).denyRequest(player, "Sender");
        }
    }

    // ==================== removeFriend ====================

    @Nested
    @DisplayName("removeFriend")
    class RemoveFriend {

        @Test
        @DisplayName("Should remove friend")
        void removeFriend() {
            command.removeFriend(player, "Friend");

            verify(friendService).removeFriend(player, "Friend");
        }
    }

    // ==================== viewRequests ====================

    @Nested
    @DisplayName("viewRequests")
    class ViewRequests {

        @Test
        @DisplayName("Should show pending requests")
        void showPendingRequests() {
            FriendRequest request1 = FriendRequest.create(
                    UUID.randomUUID(),
                    "Sender1",
                    playerUuid
            );
            FriendRequest request2 = FriendRequest.create(
                    UUID.randomUUID(),
                    "Sender2",
                    playerUuid
            );

            when(friendService.getPendingRequests(playerUuid))
                    .thenReturn(Arrays.asList(request1, request2));

            command.viewRequests(player);

            verify(player, atLeastOnce()).sendMessage(contains("好友请求"));
        }

        @Test
        @DisplayName("Should show message when no requests")
        void showNoRequests() {
            when(friendService.getPendingRequests(playerUuid))
                    .thenReturn(new ArrayList<>());

            command.viewRequests(player);

            verify(player).sendMessage(contains("没有待处理"));
        }

        @Test
        @DisplayName("Should show accept command hint for each request")
        void showAcceptHintForRequests() {
            FriendRequest request = FriendRequest.create(
                    UUID.randomUUID(),
                    "Requester",
                    playerUuid
            );

            when(friendService.getPendingRequests(playerUuid))
                    .thenReturn(Collections.singletonList(request));

            command.viewRequests(player);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            boolean hasAcceptHint = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("/friend accept Requester"));
            assertThat(hasAcceptHint).isTrue();
        }
    }

    // ==================== teleportToFriend ====================

    @Nested
    @DisplayName("teleportToFriend")
    class TeleportToFriend {

        @Test
        @DisplayName("Should teleport to online friend")
        void teleportToOnlineFriend() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));
            when(friendService.canTeleport(playerUuid)).thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                command.teleportToFriend(player, "TargetPlayer");

                verify(teleportService).teleport(eq(player), any(Location.class));
                verify(friendService).setTpCooldown(playerUuid);
            }
        }

        @Test
        @DisplayName("Should show error when tp disabled")
        void teleportDisabled() {
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(false);

            command.teleportToFriend(player, "Friend");

            verify(player).sendMessage(contains("已禁用"));
        }

        @Test
        @DisplayName("Should show error when not friends")
        void teleportNotFriends() {
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(new ArrayList<>());

            command.teleportToFriend(player, "NotFriend");

            verify(player).sendMessage(contains("不是你的好友"));
        }

        @Test
        @DisplayName("Should show error when friend offline")
        void teleportFriendOffline() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(null);

                command.teleportToFriend(player, "TargetPlayer");

                verify(player).sendMessage(contains("不在线"));
            }
        }

        @Test
        @DisplayName("Should show cooldown message when on cooldown")
        void teleportOnCooldown() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));
            when(friendService.canTeleport(playerUuid)).thenReturn(false);
            when(friendService.getRemainingCooldown(playerUuid)).thenReturn(15);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                command.teleportToFriend(player, "TargetPlayer");

                verify(player).sendMessage(contains("冷却中"));
            }
        }

        @Test
        @DisplayName("Should use direct teleport when TeleportService is null")
        void teleportWithoutService() {
            // Create command with null TeleportService
            FriendCommand noServiceCommand = new FriendCommand(friendService, null);

            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));
            when(friendService.canTeleport(playerUuid)).thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                noServiceCommand.teleportToFriend(player, "TargetPlayer");

                verify(player).teleport(any(Location.class));
                verify(friendService).setTpCooldown(playerUuid);
            }
        }

        @Test
        @DisplayName("Should send success message after teleport")
        void teleportSuccessMessage() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getConfig().isTpToFriendEnabled()).thenReturn(true);
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));
            when(friendService.canTeleport(playerUuid)).thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                command.teleportToFriend(player, "TargetPlayer");

                verify(player).sendMessage(contains("传送到"));
            }
        }
    }

    // ==================== sendMessage ====================

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("Should send private message to online friend")
        void sendMessageToOnlineFriend() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                command.sendMessage(player, "TargetPlayer", new String[]{"Hello", "World"});

                // Target should receive the message
                ArgumentCaptor<String> targetCaptor = ArgumentCaptor.forClass(String.class);
                verify(target).sendMessage(targetCaptor.capture());
                assertThat(targetCaptor.getValue()).contains("Hello World");
                assertThat(targetCaptor.getValue()).contains("TestPlayer");

                // Sender should receive confirmation
                ArgumentCaptor<String> senderCaptor = ArgumentCaptor.forClass(String.class);
                verify(player).sendMessage(senderCaptor.capture());
                assertThat(senderCaptor.getValue()).contains("Hello World");
                assertThat(senderCaptor.getValue()).contains("TargetPlayer");
            }
        }

        @Test
        @DisplayName("Should show error when messaging non-friend")
        void sendMessageToNonFriend() {
            when(friendService.getFriends(playerUuid))
                    .thenReturn(new ArrayList<>());

            command.sendMessage(player, "NonFriend", new String[]{"Hello"});

            verify(player).sendMessage(contains("不是你的好友"));
        }

        @Test
        @DisplayName("Should show error when friend is offline")
        void sendMessageToOfflineFriend() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(null);

                command.sendMessage(player, "TargetPlayer", new String[]{"Hello"});

                verify(player).sendMessage(contains("不在线"));
            }
        }

        @Test
        @DisplayName("Should include private message prefix")
        void sendMessageHasPrefix() {
            FriendshipData friendship = FriendshipData.builder()
                    .friendUuid(targetUuid.toString())
                    .friendName("TargetPlayer")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friendship));

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayer(targetUuid))
                        .thenReturn(target);

                command.sendMessage(player, "TargetPlayer", new String[]{"Test"});

                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(target).sendMessage(captor.capture());
                assertThat(captor.getValue()).contains("[私聊]");
            }
        }
    }

    // ==================== blockPlayer ====================

    @Nested
    @DisplayName("blockPlayer")
    class BlockPlayer {

        @Test
        @DisplayName("Should block online player")
        void blockOnlinePlayer() {
            when(friendService.addToBlacklist(player, target, null))
                    .thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TargetPlayer"))
                        .thenReturn(target);

                command.blockPlayer(player, "TargetPlayer");

                verify(friendService).addToBlacklist(player, target, null);
                verify(player).sendMessage(contains("加入黑名单"));
            }
        }

        @Test
        @DisplayName("Should show error when trying to block self")
        void blockSelf() {
            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TestPlayer"))
                        .thenReturn(player);

                command.blockPlayer(player, "TestPlayer");

                verify(player).sendMessage(contains("不能拉黑自己"));
            }
        }

        @Test
        @DisplayName("Should show error when already blocked")
        void blockAlreadyBlocked() {
            when(friendService.addToBlacklist(player, target, null))
                    .thenReturn(false);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TargetPlayer"))
                        .thenReturn(target);

                command.blockPlayer(player, "TargetPlayer");

                verify(player).sendMessage(contains("已在黑名单中"));
            }
        }

        @Test
        @DisplayName("Should block offline player who has played before")
        void blockOfflinePlayerPlayedBefore() {
            OfflinePlayer offlineTarget = mock(OfflinePlayer.class);
            UUID offlineUuid = UUID.randomUUID();
            when(offlineTarget.hasPlayedBefore()).thenReturn(true);
            when(offlineTarget.getUniqueId()).thenReturn(offlineUuid);

            when(friendService.addToBlacklist(playerUuid, offlineUuid, "OfflineGuy", null))
                    .thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("OfflineGuy"))
                        .thenReturn(null);
                bukkitMock.when(() -> Bukkit.getOfflinePlayer("OfflineGuy"))
                        .thenReturn(offlineTarget);

                command.blockPlayer(player, "OfflineGuy");

                verify(friendService).addToBlacklist(playerUuid, offlineUuid, "OfflineGuy", null);
                verify(player).sendMessage(contains("加入黑名单"));
            }
        }

        @Test
        @DisplayName("Should show error for offline player who never played")
        void blockOfflinePlayerNeverPlayed() {
            OfflinePlayer offlineTarget = mock(OfflinePlayer.class);
            when(offlineTarget.hasPlayedBefore()).thenReturn(false);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("NeverPlayed"))
                        .thenReturn(null);
                bukkitMock.when(() -> Bukkit.getOfflinePlayer("NeverPlayed"))
                        .thenReturn(offlineTarget);

                command.blockPlayer(player, "NeverPlayed");

                verify(player).sendMessage(contains("不存在"));
            }
        }

        @Test
        @DisplayName("Should show already blocked for offline player")
        void blockOfflinePlayerAlreadyBlocked() {
            OfflinePlayer offlineTarget = mock(OfflinePlayer.class);
            UUID offlineUuid = UUID.randomUUID();
            when(offlineTarget.hasPlayedBefore()).thenReturn(true);
            when(offlineTarget.getUniqueId()).thenReturn(offlineUuid);

            when(friendService.addToBlacklist(playerUuid, offlineUuid, "OfflineBlocked", null))
                    .thenReturn(false);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("OfflineBlocked"))
                        .thenReturn(null);
                bukkitMock.when(() -> Bukkit.getOfflinePlayer("OfflineBlocked"))
                        .thenReturn(offlineTarget);

                command.blockPlayer(player, "OfflineBlocked");

                verify(player).sendMessage(contains("已在黑名单中"));
            }
        }
    }

    // ==================== unblockPlayer ====================

    @Nested
    @DisplayName("unblockPlayer")
    class UnblockPlayer {

        @Test
        @DisplayName("Should unblock player")
        void unblockPlayer() {
            when(friendService.removeFromBlacklist(player, "BlockedPlayer"))
                    .thenReturn(true);

            command.unblockPlayer(player, "BlockedPlayer");

            verify(friendService).removeFromBlacklist(player, "BlockedPlayer");
            verify(player).sendMessage(contains("从黑名单中移除"));
        }

        @Test
        @DisplayName("Should show error when not blocked")
        void unblockNotBlocked() {
            when(friendService.removeFromBlacklist(player, "NotBlocked"))
                    .thenReturn(false);

            command.unblockPlayer(player, "NotBlocked");

            verify(player).sendMessage(contains("不在你的黑名单中"));
        }
    }

    // ==================== help ====================

    @Nested
    @DisplayName("help")
    class Help {

        @Test
        @DisplayName("Should show help message")
        void showHelp() {
            command.help(player);

            verify(player, atLeastOnce()).sendMessage(contains("好友系统帮助"));
        }

        @Test
        @DisplayName("Should show all subcommand descriptions")
        void showAllSubcommands() {
            command.help(player);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            List<String> messages = captor.getAllValues();

            // Should contain key subcommands
            boolean hasAdd = messages.stream().anyMatch(m -> m.contains("/friend add"));
            boolean hasRemove = messages.stream().anyMatch(m -> m.contains("/friend remove"));
            boolean hasTp = messages.stream().anyMatch(m -> m.contains("/friend tp"));
            boolean hasMsg = messages.stream().anyMatch(m -> m.contains("/friend msg"));
            boolean hasBlock = messages.stream().anyMatch(m -> m.contains("/friend block"));
            boolean hasUnblock = messages.stream().anyMatch(m -> m.contains("/friend unblock"));

            assertThat(hasAdd).isTrue();
            assertThat(hasRemove).isTrue();
            assertThat(hasTp).isTrue();
            assertThat(hasMsg).isTrue();
            assertThat(hasBlock).isTrue();
            assertThat(hasUnblock).isTrue();
        }
    }

    // ==================== handleHelp ====================

    @Nested
    @DisplayName("handleHelp")
    class HandleHelp {

        @Test
        @DisplayName("Should call help for Player sender")
        void handleHelpPlayer() {
            command.handleHelp(player);

            verify(player, atLeastOnce()).sendMessage(anyString());
        }

        @Test
        @DisplayName("Should not call help for non-Player sender")
        void handleHelpNonPlayer() {
            CommandSender consoleSender = mock(CommandSender.class);

            command.handleHelp(consoleSender);

            verify(consoleSender, never()).sendMessage(anyString());
        }
    }

    // ==================== blockPlayer - were friends notification ====================

    @Nested
    @DisplayName("blockPlayer - were friends")
    class BlockPlayerWereFriends {

        @Test
        @DisplayName("Should notify about auto friendship removal when blocking a friend")
        void blockFriendShowsAutoRemoval() {
            when(friendService.addToBlacklist(player, target, null)).thenReturn(true);
            when(friendService.areFriends(playerUuid, targetUuid)).thenReturn(true);

            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(() -> Bukkit.getPlayerExact("TargetPlayer"))
                        .thenReturn(target);

                command.blockPlayer(player, "TargetPlayer");

                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(player, atLeastOnce()).sendMessage(captor.capture());
                boolean hasAutoRemoval = captor.getAllValues().stream()
                        .anyMatch(msg -> msg.contains("自动解除好友"));
                assertThat(hasAutoRemoval).isTrue();
            }
        }
    }

    // ==================== onTabComplete ====================

    @Nested
    @DisplayName("onTabComplete")
    class OnTabComplete {

        @Test
        @DisplayName("Should suggest subcommands for first argument")
        void suggestSubcommands() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{""});

            assertThat(result).contains("list", "add", "accept", "deny", "remove",
                    "tp", "msg", "requests", "block", "unblock", "blocklist", "help");
        }

        @Test
        @DisplayName("Should filter subcommands by prefix")
        void filterSubcommandsByPrefix() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"bl"});

            assertThat(result).contains("block", "blocklist");
            assertThat(result).doesNotContain("list", "add", "help");
        }

        @Test
        @DisplayName("Should suggest online players for add subcommand")
        void suggestPlayersForAdd() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            Player other1 = UltiSocialTestHelper.createMockPlayer("Player1", UUID.randomUUID());
            Player other2 = UltiSocialTestHelper.createMockPlayer("Player2", UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) Arrays.asList(player, other1, other2);
            doReturn(onlinePlayers).when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"add", ""});

            assertThat(result).contains("Player1", "Player2");
            assertThat(result).doesNotContain("TestPlayer"); // Should not suggest self
        }

        @Test
        @DisplayName("Should suggest online players for block subcommand")
        void suggestPlayersForBlock() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            Player other1 = UltiSocialTestHelper.createMockPlayer("Target1", UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) Arrays.asList(player, other1);
            doReturn(onlinePlayers).when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"block", ""});

            assertThat(result).contains("Target1");
            assertThat(result).doesNotContain("TestPlayer");
        }

        @Test
        @DisplayName("Should suggest pending request senders for accept")
        void suggestSendersForAccept() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            FriendRequest req = FriendRequest.create(UUID.randomUUID(), "Requester1", playerUuid);
            when(friendService.getPendingRequests(playerUuid))
                    .thenReturn(Collections.singletonList(req));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"accept", ""});

            assertThat(result).contains("Requester1");
        }

        @Test
        @DisplayName("Should suggest pending request senders for deny")
        void suggestSendersForDeny() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            FriendRequest req = FriendRequest.create(UUID.randomUUID(), "Requester2", playerUuid);
            when(friendService.getPendingRequests(playerUuid))
                    .thenReturn(Collections.singletonList(req));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"deny", ""});

            assertThat(result).contains("Requester2");
        }

        @Test
        @DisplayName("Should suggest friends for remove subcommand")
        void suggestFriendsForRemove() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(UUID.randomUUID().toString())
                    .friendName("MyFriend")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"remove", ""});

            assertThat(result).contains("MyFriend");
        }

        @Test
        @DisplayName("Should suggest friends for tp subcommand")
        void suggestFriendsForTp() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(UUID.randomUUID().toString())
                    .friendName("TpFriend")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"tp", ""});

            assertThat(result).contains("TpFriend");
        }

        @Test
        @DisplayName("Should suggest friends for msg subcommand")
        void suggestFriendsForMsg() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(UUID.randomUUID().toString())
                    .friendName("MsgFriend")
                    .build();
            when(friendService.getFriends(playerUuid))
                    .thenReturn(Collections.singletonList(friend));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"msg", ""});

            assertThat(result).contains("MsgFriend");
        }

        @Test
        @DisplayName("Should suggest blocked users for unblock subcommand")
        void suggestBlockedForUnblock() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            BlacklistData blocked = BlacklistData.builder()
                    .blockedUuid(UUID.randomUUID().toString())
                    .blockedName("BlockedUser")
                    .build();
            when(friendService.getBlacklist(playerUuid))
                    .thenReturn(Collections.singletonList(blocked));

            List<String> result = command.onTabComplete(player, cmd, "friend", new String[]{"unblock", ""});

            assertThat(result).contains("BlockedUser");
        }

        @Test
        @DisplayName("Should return empty for non-player sender")
        void emptyForNonPlayer() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);
            CommandSender consoleSender = mock(CommandSender.class);

            List<String> result = command.onTabComplete(consoleSender, cmd, "friend", new String[]{"add", ""});

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for third argument")
        void emptyForThirdArg() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            List<String> result = command.onTabComplete(player, cmd, "friend",
                    new String[]{"msg", "Friend", ""});

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for unknown subcommand")
        void emptyForUnknownSubcommand() {
            org.bukkit.command.Command cmd = mock(org.bukkit.command.Command.class);

            List<String> result = command.onTabComplete(player, cmd, "friend",
                    new String[]{"xyz", ""});

            assertThat(result).isEmpty();
        }
    }

    // ==================== filterStartsWith ====================

    @Nested
    @DisplayName("filterStartsWith (via reflection)")
    class FilterStartsWith {

        @Test
        @DisplayName("Should filter suggestions by prefix")
        void filterByPrefix() throws Exception {
            java.lang.reflect.Method method = FriendCommand.class.getDeclaredMethod(
                    "filterStartsWith", List.class, String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(command,
                    Arrays.asList("add", "accept", "block", "blocklist"), "a");

            assertThat(result).containsExactly("add", "accept");
        }

        @Test
        @DisplayName("Should return all when prefix is empty")
        void returnAllWhenEmpty() throws Exception {
            java.lang.reflect.Method method = FriendCommand.class.getDeclaredMethod(
                    "filterStartsWith", List.class, String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(command,
                    Arrays.asList("add", "block"), "");

            assertThat(result).containsExactly("add", "block");
        }

        @Test
        @DisplayName("Should return all when prefix is null")
        void returnAllWhenNull() throws Exception {
            java.lang.reflect.Method method = FriendCommand.class.getDeclaredMethod(
                    "filterStartsWith", List.class, String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(command,
                    Arrays.asList("add", "block"), null);

            assertThat(result).containsExactly("add", "block");
        }

        @Test
        @DisplayName("Should be case insensitive")
        void caseInsensitive() throws Exception {
            java.lang.reflect.Method method = FriendCommand.class.getDeclaredMethod(
                    "filterStartsWith", List.class, String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(command,
                    Arrays.asList("Add", "Block"), "a");

            assertThat(result).containsExactly("Add");
        }
    }
}
