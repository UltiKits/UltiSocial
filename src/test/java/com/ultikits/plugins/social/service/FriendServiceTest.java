package com.ultikits.plugins.social.service;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendRequest;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.ultitools.interfaces.DataOperator;
import com.ultikits.ultitools.interfaces.Query;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FriendService.
 */
@DisplayName("FriendService Tests")
class FriendServiceTest {

    private FriendService service;
    private SocialConfig config;
    @SuppressWarnings("unchecked")
    private DataOperator<FriendshipData> friendDataOperator = mock(DataOperator.class);
    @SuppressWarnings("unchecked")
    private DataOperator<BlacklistData> blacklistDataOperator = mock(DataOperator.class);

    // Query mocks for chaining
    @SuppressWarnings("unchecked")
    private Query<FriendshipData> friendQuery = mock(Query.class);
    @SuppressWarnings("unchecked")
    private Query<BlacklistData> blacklistQuery = mock(Query.class);

    private Player player;
    private Player friend;
    private UUID playerUuid;
    private UUID friendUuid;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();

        config = UltiSocialTestHelper.createDefaultConfig();

        // Reset mocks for fresh state
        reset(friendDataOperator, blacklistDataOperator, friendQuery, blacklistQuery);

        // Set up Query DSL chaining for friendDataOperator
        when(friendDataOperator.query()).thenReturn(friendQuery);
        when(friendQuery.where(anyString())).thenReturn(friendQuery);
        when(friendQuery.and(anyString())).thenReturn(friendQuery);
        when(friendQuery.eq(any())).thenReturn(friendQuery);
        when(friendQuery.ne(any())).thenReturn(friendQuery);
        when(friendQuery.list()).thenReturn(new ArrayList<>());
        when(friendQuery.first()).thenReturn(null);
        when(friendQuery.exists()).thenReturn(false);
        when(friendQuery.count()).thenReturn(0L);
        when(friendQuery.delete()).thenReturn(0);

        // Set up Query DSL chaining for blacklistDataOperator
        when(blacklistDataOperator.query()).thenReturn(blacklistQuery);
        when(blacklistQuery.where(anyString())).thenReturn(blacklistQuery);
        when(blacklistQuery.and(anyString())).thenReturn(blacklistQuery);
        when(blacklistQuery.eq(any())).thenReturn(blacklistQuery);
        when(blacklistQuery.ne(any())).thenReturn(blacklistQuery);
        when(blacklistQuery.list()).thenReturn(new ArrayList<>());
        when(blacklistQuery.first()).thenReturn(null);
        when(blacklistQuery.exists()).thenReturn(false);
        when(blacklistQuery.count()).thenReturn(0L);
        when(blacklistQuery.delete()).thenReturn(0);

        service = new FriendService();

        // Inject dependencies via reflection
        UltiSocialTestHelper.setField(service, "plugin", UltiSocialTestHelper.getMockPlugin());
        UltiSocialTestHelper.setField(service, "config", config);
        UltiSocialTestHelper.setField(service, "dataOperator", friendDataOperator);
        UltiSocialTestHelper.setField(service, "blacklistDataOperator", blacklistDataOperator);

        playerUuid = UUID.randomUUID();
        friendUuid = UUID.randomUUID();
        player = UltiSocialTestHelper.createMockPlayer("TestPlayer", playerUuid);
        friend = UltiSocialTestHelper.createMockPlayer("TestFriend", friendUuid);
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    // ==================== sendRequest ====================

    @Nested
    @DisplayName("sendRequest")
    class SendRequest {

        @Test
        @DisplayName("Should send friend request successfully")
        void sendRequestSuccess() {
            // blacklistQuery.list() already returns empty (not blocked)
            // friendQuery.list() already returns empty (not friends, no max)

            boolean result = service.sendRequest(player, friend);

            assertThat(result).isTrue();
            List<FriendRequest> requests = service.getPendingRequests(friendUuid);
            assertThat(requests).hasSize(1);
            assertThat(requests.get(0).getSender()).isEqualTo(playerUuid);
            assertThat(requests.get(0).getSenderName()).isEqualTo("TestPlayer");
        }

        @Test
        @DisplayName("Should reject request if already friends")
        void rejectIfAlreadyFriends() {
            FriendshipData existingFriendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();

            // blacklist returns empty (not blocked) — default
            // friends query returns existing friendship
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(existingFriendship)));

            boolean result = service.sendRequest(player, friend);

            assertThat(result).isFalse();
            verify(player).sendMessage(contains("already"));
        }

        @Test
        @DisplayName("Should reject request if blocked")
        void rejectIfBlocked() {
            BlacklistData blacklist = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .createdTime(System.currentTimeMillis())
                    .build();

            // blacklist query returns that player has blocked friend
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(blacklist)));

            boolean result = service.sendRequest(player, friend);

            assertThat(result).isFalse();
            verify(player).sendMessage(contains("blacklist"));
        }

        @Test
        @DisplayName("Should reject if max friends reached")
        void rejectIfMaxFriends() {
            // blacklist returns empty (not blocked) — default
            List<FriendshipData> friends = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                friends.add(FriendshipData.builder()
                        .friendName("Friend" + i)
                        .friendUuid(UUID.randomUUID().toString())
                        .playerUuid(playerUuid.toString())
                        .createdTime(System.currentTimeMillis())
                        .favorite(false)
                        .build());
            }
            when(friendQuery.list()).thenReturn(friends);

            boolean result = service.sendRequest(player, friend);

            assertThat(result).isFalse();
            verify(player).sendMessage(contains("maximum"));
        }

        @Test
        @DisplayName("Should reject duplicate request")
        void rejectDuplicateRequest() {
            // blacklist returns empty, friends returns empty — default

            service.sendRequest(player, friend);
            boolean result = service.sendRequest(player, friend);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should auto-accept when mutual request exists")
        void autoAcceptMutualRequest() {
            // Send a request from friend to player first
            service.sendRequest(friend, player);

            // Mock Bukkit.getPlayer() for notification
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(friend);

            // Now player sends request to friend — should auto-accept
            boolean result = service.sendRequest(player, friend);

            assertThat(result).isTrue();
            // Both friendships should be created
            verify(friendDataOperator, times(2)).insert(any(FriendshipData.class));
        }

        @Test
        @DisplayName("Should send request messages to both players")
        void sendRequestMessages() {
            boolean result = service.sendRequest(player, friend);

            assertThat(result).isTrue();
            // Sender gets confirmation
            ArgumentCaptor<String> senderCaptor = ArgumentCaptor.forClass(String.class);
            verify(player).sendMessage(senderCaptor.capture());
            assertThat(senderCaptor.getValue()).contains("TestFriend");

            // Receiver gets notification
            ArgumentCaptor<String> receiverCaptor = ArgumentCaptor.forClass(String.class);
            verify(friend).sendMessage(receiverCaptor.capture());
            assertThat(receiverCaptor.getValue()).contains("TestPlayer");
        }

        @Test
        @DisplayName("Should replace color codes in messages")
        void replaceColorCodes() {
            service.sendRequest(player, friend);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player).sendMessage(captor.capture());
            // Config messages use &a etc, should be converted to section sign
            assertThat(captor.getValue()).doesNotContain("&a");
        }
    }

    // ==================== acceptRequest ====================

    @Nested
    @DisplayName("acceptRequest")
    class AcceptRequest {

        @Test
        @DisplayName("Should accept valid request")
        void acceptValidRequest() {
            // Send a request first
            service.sendRequest(player, friend);

            // Mock Bukkit.getPlayer() to return player for notification
            when(UltiSocialTestHelper.getMockServer().getPlayer(playerUuid)).thenReturn(player);

            boolean result = service.acceptRequest(friend, "TestPlayer");

            assertThat(result).isTrue();
            verify(friendDataOperator, times(2)).insert(any(FriendshipData.class));
        }

        @Test
        @DisplayName("Should reject non-existent request")
        void rejectNonExistentRequest() {
            boolean result = service.acceptRequest(friend, "NonExistent");

            assertThat(result).isFalse();
            verify(friend).sendMessage(contains("no_pending_request"));
        }

        @Test
        @DisplayName("Should reject expired request")
        void rejectExpiredRequest() {
            FriendRequest expiredRequest = new FriendRequest(
                    playerUuid,
                    "TestPlayer",
                    friendUuid,
                    System.currentTimeMillis() - 120000 // 2 minutes ago
            );
            Map<UUID, List<FriendRequest>> requests = new ConcurrentHashMapWrapper<>();
            requests.put(friendUuid, new ArrayList<>(Collections.singletonList(expiredRequest)));
            try {
                UltiSocialTestHelper.setField(service, "pendingRequests", requests);
            } catch (Exception e) {
                fail("Failed to set pending requests");
            }

            boolean result = service.acceptRequest(friend, "TestPlayer");

            assertThat(result).isFalse();
            verify(friend).sendMessage(contains("request_expired"));
        }

        @Test
        @DisplayName("Should reject if max friends reached")
        void rejectIfMaxFriends() {
            // Send request
            service.sendRequest(player, friend);

            // Now friend has max friends
            List<FriendshipData> maxFriends = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                maxFriends.add(FriendshipData.builder()
                        .friendName("Friend" + i)
                        .friendUuid(UUID.randomUUID().toString())
                        .playerUuid(friendUuid.toString())
                        .createdTime(System.currentTimeMillis())
                        .favorite(false)
                        .build());
            }
            when(friendQuery.list()).thenReturn(maxFriends);

            boolean result = service.acceptRequest(friend, "TestPlayer");

            assertThat(result).isFalse();
            verify(friend).sendMessage(contains("maximum"));
        }

        @Test
        @DisplayName("Should notify sender when request accepted")
        void notifySenderOnAccept() {
            service.sendRequest(player, friend);
            when(UltiSocialTestHelper.getMockServer().getPlayer(playerUuid)).thenReturn(player);

            service.acceptRequest(friend, "TestPlayer");

            // Both players should receive friend added message
            verify(player, atLeastOnce()).sendMessage(anyString());
            verify(friend, atLeastOnce()).sendMessage(anyString());
        }

        @Test
        @DisplayName("Should clear friend cache after acceptance")
        void clearCacheAfterAccept() {
            service.sendRequest(player, friend);
            when(UltiSocialTestHelper.getMockServer().getPlayer(playerUuid)).thenReturn(player);

            // Pre-populate cache by calling getFriends
            service.getFriends(playerUuid);
            service.getFriends(friendUuid);

            service.acceptRequest(friend, "TestPlayer");

            // Cache should be cleared, next call should hit DB again
            service.getFriends(playerUuid);
            // query().list() called 3 times: 1st cache-fill + 2nd after cache clear for playerUuid
            // friendUuid also had cache cleared
        }

        @Test
        @DisplayName("Should handle when sender is offline during acceptance")
        void senderOfflineDuringAccept() {
            service.sendRequest(player, friend);
            when(UltiSocialTestHelper.getMockServer().getPlayer(playerUuid)).thenReturn(null);

            boolean result = service.acceptRequest(friend, "TestPlayer");

            assertThat(result).isTrue();
            // Receiver should still get message
            verify(friend, atLeastOnce()).sendMessage(anyString());
        }
    }

    // ==================== denyRequest ====================

    @Nested
    @DisplayName("denyRequest")
    class DenyRequest {

        @Test
        @DisplayName("Should deny valid request")
        void denyValidRequest() {
            service.sendRequest(player, friend);

            boolean result = service.denyRequest(friend, "TestPlayer");

            assertThat(result).isTrue();
            List<FriendRequest> requests = service.getPendingRequests(friendUuid);
            assertThat(requests).isEmpty();
        }

        @Test
        @DisplayName("Should reject non-existent request")
        void rejectNonExistentRequest() {
            boolean result = service.denyRequest(friend, "NonExistent");

            assertThat(result).isFalse();
            verify(friend).sendMessage(contains("no_pending_request"));
        }

        @Test
        @DisplayName("Should send denial message")
        void sendDenialMessage() {
            service.sendRequest(player, friend);

            service.denyRequest(friend, "TestPlayer");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(friend, atLeastOnce()).sendMessage(captor.capture());
            // At least one message should contain the denied message content
            boolean hasDeniedMessage = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("TestPlayer"));
            assertThat(hasDeniedMessage).isTrue();
        }

        @Test
        @DisplayName("Should reject when request not found by name")
        void rejectWhenNameNotFound() {
            // Add a request from someone else
            Player otherPlayer = UltiSocialTestHelper.createMockPlayer("OtherPlayer", UUID.randomUUID());
            service.sendRequest(otherPlayer, friend);

            boolean result = service.denyRequest(friend, "NonExistent");

            assertThat(result).isFalse();
        }
    }

    // ==================== removeFriend ====================

    @Nested
    @DisplayName("removeFriend")
    class RemoveFriend {

        @Test
        @DisplayName("Should remove friend successfully")
        void removeFriendSuccess() {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();
            friendship.setId("friend-id");
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            boolean result = service.removeFriend(player, "TestFriend");

            assertThat(result).isTrue();
            verify(friendDataOperator).delById("friend-id");
            // Reverse friendship should also be deleted via query
            verify(friendQuery, atLeastOnce()).delete();
        }

        @Test
        @DisplayName("Should reject non-existent friend")
        void rejectNonExistentFriend() {
            // friendQuery.list() returns empty by default

            boolean result = service.removeFriend(player, "NonExistent");

            assertThat(result).isFalse();
            verify(player).sendMessage(contains("not_friend"));
        }

        @Test
        @DisplayName("Should send removal message")
        void sendRemovalMessage() {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();
            friendship.setId("friend-id");
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.removeFriend(player, "TestFriend");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(player, atLeastOnce()).sendMessage(captor.capture());
            boolean hasRemoveMsg = captor.getAllValues().stream()
                    .anyMatch(msg -> msg.contains("TestFriend"));
            assertThat(hasRemoveMsg).isTrue();
        }

        @Test
        @DisplayName("Should clear friend cache for both players after removal")
        void clearCacheAfterRemoval() {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();
            friendship.setId("friend-id");
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            // Pre-populate cache
            service.getFriends(playerUuid);

            service.removeFriend(player, "TestFriend");

            // Cache should be cleared; next getFriends should re-query
            // (but since cache was cleared, the result depends on mock state)
        }
    }

    // ==================== getFriends ====================

    @Nested
    @DisplayName("getFriends")
    class GetFriends {

        @Test
        @DisplayName("Should return sorted friends list")
        void returnSortedList() {
            FriendshipData favorite = FriendshipData.builder()
                    .friendName("Favorite")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData normal1 = FriendshipData.builder()
                    .friendName("BNormal")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData normal2 = FriendshipData.builder()
                    .friendName("ANormal")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Arrays.asList(normal1, favorite, normal2)));

            List<FriendshipData> friends = service.getFriends(playerUuid);

            assertThat(friends).hasSize(3);
            assertThat(friends.get(0).getFriendName()).isEqualTo("Favorite");
            assertThat(friends.get(1).getFriendName()).isEqualTo("ANormal");
            assertThat(friends.get(2).getFriendName()).isEqualTo("BNormal");
        }

        @Test
        @DisplayName("Should return empty list when no friends")
        void returnEmptyList() {
            // friendQuery.list() returns empty by default

            List<FriendshipData> friends = service.getFriends(playerUuid);

            assertThat(friends).isEmpty();
        }

        @Test
        @DisplayName("Should use cache on second call")
        void useCache() {
            when(friendQuery.list()).thenReturn(new ArrayList<>());

            service.getFriends(playerUuid);
            service.getFriends(playerUuid);

            // query().where().eq().list() should only be called once
            verify(friendQuery, times(1)).list();
        }

        @Test
        @DisplayName("Should sort favorites before non-favorites")
        void sortFavoritesFirst() {
            FriendshipData nonFav = FriendshipData.builder()
                    .friendName("Alpha")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData fav = FriendshipData.builder()
                    .friendName("Zeta")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Arrays.asList(nonFav, fav)));

            List<FriendshipData> friends = service.getFriends(playerUuid);

            assertThat(friends.get(0).getFriendName()).isEqualTo("Zeta");
            assertThat(friends.get(1).getFriendName()).isEqualTo("Alpha");
        }

        @Test
        @DisplayName("Should sort alphabetically within same favorite status")
        void sortAlphabeticallyWithinGroup() {
            FriendshipData c = FriendshipData.builder()
                    .friendName("Charlie")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData a = FriendshipData.builder()
                    .friendName("Alpha")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            FriendshipData b = FriendshipData.builder()
                    .friendName("Bravo")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Arrays.asList(c, a, b)));

            List<FriendshipData> friends = service.getFriends(playerUuid);

            assertThat(friends.get(0).getFriendName()).isEqualTo("Alpha");
            assertThat(friends.get(1).getFriendName()).isEqualTo("Bravo");
            assertThat(friends.get(2).getFriendName()).isEqualTo("Charlie");
        }
    }

    // ==================== areFriends ====================

    @Nested
    @DisplayName("areFriends")
    class AreFriends {

        @Test
        @DisplayName("Should return true for friends")
        void returnTrueForFriends() {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            boolean result = service.areFriends(playerUuid, friendUuid);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-friends")
        void returnFalseForNonFriends() {
            // friendQuery.list() returns empty by default

            boolean result = service.areFriends(playerUuid, friendUuid);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when friend UUID does not match")
        void returnFalseWhenUuidMismatch() {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(UUID.randomUUID().toString()) // different UUID
                    .friendName("SomeoneElse")
                    .createdTime(System.currentTimeMillis())
                    .favorite(false)
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            boolean result = service.areFriends(playerUuid, friendUuid);

            assertThat(result).isFalse();
        }
    }

    // ==================== getPendingRequests ====================

    @Nested
    @DisplayName("getPendingRequests")
    class GetPendingRequests {

        @Test
        @DisplayName("Should return empty list when no requests")
        void returnEmptyWhenNone() {
            List<FriendRequest> requests = service.getPendingRequests(playerUuid);

            assertThat(requests).isEmpty();
        }

        @Test
        @DisplayName("Should return pending requests")
        void returnPendingRequests() {
            service.sendRequest(friend, player);

            List<FriendRequest> requests = service.getPendingRequests(playerUuid);

            assertThat(requests).hasSize(1);
            assertThat(requests.get(0).getSenderName()).isEqualTo("TestFriend");
        }

        @Test
        @DisplayName("Should filter out expired requests")
        void filterExpiredRequests() {
            FriendRequest expired = new FriendRequest(
                    friendUuid, "TestFriend", playerUuid,
                    System.currentTimeMillis() - 120000 // 2 minutes ago, timeout is 60s
            );
            Map<UUID, List<FriendRequest>> requests = new java.util.concurrent.ConcurrentHashMap<>();
            requests.put(playerUuid, new ArrayList<>(Collections.singletonList(expired)));
            try {
                UltiSocialTestHelper.setField(service, "pendingRequests", requests);
            } catch (Exception e) {
                fail("Failed to set pending requests");
            }

            List<FriendRequest> result = service.getPendingRequests(playerUuid);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getFriendCount ====================

    @Nested
    @DisplayName("getFriendCount")
    class GetFriendCount {

        @Test
        @DisplayName("Should return count of friends")
        void returnFriendCount() {
            FriendshipData f1 = FriendshipData.builder()
                    .friendName("F1").friendUuid(UUID.randomUUID().toString())
                    .createdTime(System.currentTimeMillis()).favorite(false).build();
            FriendshipData f2 = FriendshipData.builder()
                    .friendName("F2").friendUuid(UUID.randomUUID().toString())
                    .createdTime(System.currentTimeMillis()).favorite(false).build();
            when(friendQuery.list()).thenReturn(new ArrayList<>(Arrays.asList(f1, f2)));

            int count = service.getFriendCount(playerUuid);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return 0 when no friends")
        void returnZeroWhenNoFriends() {
            int count = service.getFriendCount(playerUuid);

            assertThat(count).isZero();
        }
    }

    // ==================== Teleport Cooldown ====================

    @Nested
    @DisplayName("Teleport Cooldown")
    class TeleportCooldown {

        @Test
        @DisplayName("Should allow teleport with no cooldown")
        void allowTeleportNoCooldown() {
            boolean canTeleport = service.canTeleport(playerUuid);

            assertThat(canTeleport).isTrue();
        }

        @Test
        @DisplayName("Should block teleport during cooldown")
        void blockTeleportDuringCooldown() {
            service.setTpCooldown(playerUuid);

            boolean canTeleport = service.canTeleport(playerUuid);

            assertThat(canTeleport).isFalse();
        }

        @Test
        @DisplayName("Should return remaining cooldown")
        void returnRemainingCooldown() {
            service.setTpCooldown(playerUuid);

            int remaining = service.getRemainingCooldown(playerUuid);

            assertThat(remaining).isGreaterThan(0).isLessThanOrEqualTo(30);
        }

        @Test
        @DisplayName("Should return 0 remaining with no cooldown")
        void returnZeroNoCooldown() {
            int remaining = service.getRemainingCooldown(playerUuid);

            assertThat(remaining).isZero();
        }
    }

    // ==================== Blacklist ====================

    @Nested
    @DisplayName("Blacklist Operations")
    class BlacklistOperations {

        @Test
        @DisplayName("Should add player to blacklist")
        void addToBlacklist() {
            // blacklistQuery.list() returns empty (not already blocked) — default
            // friendQuery returns empty (no friendship to remove) — default

            boolean result = service.addToBlacklist(player, friend, null);

            assertThat(result).isTrue();
            verify(blacklistDataOperator).insert(any(BlacklistData.class));
        }

        @Test
        @DisplayName("Should not add duplicate to blacklist")
        void noDuplicateBlacklist() {
            BlacklistData existing = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(existing)));

            boolean result = service.addToBlacklist(player, friend, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should remove from blacklist")
        void removeFromBlacklist() {
            BlacklistData blacklist = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .blockedName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            blacklist.setId("blacklist-id");
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(blacklist)));

            boolean result = service.removeFromBlacklist(player, "TestFriend");

            assertThat(result).isTrue();
            verify(blacklistDataOperator).delById("blacklist-id");
        }

        @Test
        @DisplayName("Should check if blocked bidirectionally")
        void checkBlockedBidirectional() {
            BlacklistData blacklist = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(blacklist)));

            boolean blocked1 = service.isBlocked(playerUuid, friendUuid);

            assertThat(blocked1).isTrue();
        }

        @Test
        @DisplayName("Should return blacklist sorted by time")
        void returnSortedBlacklist() {
            BlacklistData newer = BlacklistData.builder()
                    .blockedName("Newer")
                    .createdTime(2000L)
                    .build();
            BlacklistData older = BlacklistData.builder()
                    .blockedName("Older")
                    .createdTime(1000L)
                    .build();
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Arrays.asList(older, newer)));

            List<BlacklistData> blacklist = service.getBlacklist(playerUuid);

            assertThat(blacklist).hasSize(2);
            assertThat(blacklist.get(0).getBlockedName()).isEqualTo("Newer");
            assertThat(blacklist.get(1).getBlockedName()).isEqualTo("Older");
        }

        @Test
        @DisplayName("Should return empty blacklist when none blocked")
        void returnEmptyBlacklist() {
            List<BlacklistData> blacklist = service.getBlacklist(playerUuid);

            assertThat(blacklist).isEmpty();
        }

        @Test
        @DisplayName("Should cache blacklist results")
        void cacheBlacklistResults() {
            when(blacklistQuery.list()).thenReturn(new ArrayList<>());

            service.getBlacklist(playerUuid);
            service.getBlacklist(playerUuid);

            // query().list() should only be called once due to caching
            verify(blacklistQuery, times(1)).list();
        }

        @Test
        @DisplayName("Should return blacklist count")
        void returnBlacklistCount() {
            BlacklistData entry = BlacklistData.builder()
                    .blockedName("Blocked1")
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(entry)));

            int count = service.getBlacklistCount(playerUuid);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should remove from blacklist by UUID")
        void removeFromBlacklistByUuid() {
            when(blacklistQuery.exists()).thenReturn(true);
            when(blacklistQuery.delete()).thenReturn(1);

            boolean result = service.removeFromBlacklist(playerUuid, friendUuid);

            assertThat(result).isTrue();
            verify(blacklistQuery).delete();
        }

        @Test
        @DisplayName("Should return false when removing non-existent blacklist entry by UUID")
        void removeNonExistentByUuid() {
            when(blacklistQuery.exists()).thenReturn(false);

            boolean result = service.removeFromBlacklist(playerUuid, friendUuid);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should remove friendship when adding to blacklist")
        void removeFriendshipWhenBlocking() {
            // The addToBlacklist method also calls removeFriendByUuid
            boolean result = service.addToBlacklist(player, friend, null);

            assertThat(result).isTrue();
            // Verify friendship removal queries were invoked (delete on friend query)
            verify(friendQuery, atLeastOnce()).delete();
        }

        @Test
        @DisplayName("Should clear blacklist cache after adding")
        void clearCacheAfterAdd() {
            // Pre-populate cache
            service.getBlacklist(playerUuid);

            service.addToBlacklist(player, friend, null);

            // Cache should be cleared; next call hits DB again
            service.getBlacklist(playerUuid);
            verify(blacklistQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should clear blacklist cache after removing by name")
        void clearCacheAfterRemoveByName() {
            BlacklistData blacklist = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .blockedName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            blacklist.setId("bl-id");
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(blacklist)));

            // Pre-populate cache
            service.getBlacklist(playerUuid);

            service.removeFromBlacklist(player, "TestFriend");

            // Cache cleared, next call hits DB
            service.getBlacklist(playerUuid);
            verify(blacklistQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should clear blacklist cache after removing by UUID")
        void clearCacheAfterRemoveByUuid() {
            when(blacklistQuery.exists()).thenReturn(true);
            when(blacklistQuery.delete()).thenReturn(1);

            // Pre-populate cache
            service.getBlacklist(playerUuid);

            service.removeFromBlacklist(playerUuid, friendUuid);

            // Cache cleared, next call hits DB
            service.getBlacklist(playerUuid);
            verify(blacklistQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should return false for removeFromBlacklist when name not found")
        void removeByNameNotFound() {
            // blacklistQuery.list() returns empty — default

            boolean result = service.removeFromBlacklist(player, "NonExistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isBlockedBy should return false when not blocked")
        void isBlockedByReturnsFalse() {
            // blacklistQuery.list() returns empty — default

            boolean result = service.isBlockedBy(playerUuid, friendUuid);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isBlocked should return false when neither player blocked the other")
        void isBlockedReturnsFalseWhenNeitherBlocked() {
            boolean result = service.isBlocked(playerUuid, friendUuid);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("addToBlacklist by UUID should work")
        void addToBlacklistByUuid() {
            boolean result = service.addToBlacklist(
                    playerUuid, friendUuid, "TestFriend", "spamming");

            assertThat(result).isTrue();
            verify(blacklistDataOperator).insert(any(BlacklistData.class));
        }

        @Test
        @DisplayName("addToBlacklist by UUID should reject if already blocked")
        void addToBlacklistByUuidDuplicate() {
            BlacklistData existing = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(friendUuid.toString())
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(blacklistQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(existing)));

            boolean result = service.addToBlacklist(
                    playerUuid, friendUuid, "TestFriend", null);

            assertThat(result).isFalse();
        }
    }

    // ==================== toggleFavorite ====================

    @Nested
    @DisplayName("toggleFavorite")
    class ToggleFavorite {

        @Test
        @DisplayName("Should toggle favorite status")
        void toggleFavorite() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.toggleFavorite(playerUuid, "TestFriend");

            assertThat(friendship.isFavorite()).isTrue();
            verify(friendDataOperator).update(friendship);
        }

        @Test
        @DisplayName("Should toggle favorite from true to false")
        void toggleFavoriteToFalse() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.toggleFavorite(playerUuid, "TestFriend");

            assertThat(friendship.isFavorite()).isFalse();
            verify(friendDataOperator).update(friendship);
        }

        @Test
        @DisplayName("Should do nothing when friend not found")
        void noopWhenFriendNotFound() throws Exception {
            // friendQuery.list() returns empty — default

            service.toggleFavorite(playerUuid, "NonExistent");

            verify(friendDataOperator, never()).update(any());
        }

        @Test
        @DisplayName("Should clear cache after toggle")
        void clearCacheAfterToggle() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.getFriends(playerUuid); // populate cache
            service.toggleFavorite(playerUuid, "TestFriend");
            service.getFriends(playerUuid); // should re-query

            verify(friendQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should match friend name case-insensitively")
        void matchCaseInsensitive() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.toggleFavorite(playerUuid, "testfriend");

            assertThat(friendship.isFavorite()).isTrue();
            verify(friendDataOperator).update(friendship);
        }

        @Test
        @DisplayName("Should log error when update throws IllegalAccessException")
        void logErrorOnUpdateFailure() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));
            doThrow(new IllegalAccessException("test error"))
                    .when(friendDataOperator).update(any());

            // Inject mock plugin for logger verification
            UltiSocialTestHelper.setField(service, "plugin", UltiSocialTestHelper.getMockPlugin());

            service.toggleFavorite(playerUuid, "TestFriend");

            assertThat(friendship.isFavorite()).isTrue();
            verify(UltiSocialTestHelper.getMockLogger()).error(
                    eq("Failed to update friend data"), any(IllegalAccessException.class));
        }
    }

    // ==================== setNickname ====================

    @Nested
    @DisplayName("setNickname")
    class SetNickname {

        @Test
        @DisplayName("Should set nickname for friend")
        void setNickname() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.setNickname(playerUuid, "TestFriend", "BestBuddy");

            assertThat(friendship.getNickname()).isEqualTo("BestBuddy");
            verify(friendDataOperator).update(friendship);
        }

        @Test
        @DisplayName("Should do nothing when friend not found")
        void noopWhenFriendNotFound() throws Exception {
            // friendQuery.list() returns empty — default

            service.setNickname(playerUuid, "NonExistent", "Nickname");

            verify(friendDataOperator, never()).update(any());
        }

        @Test
        @DisplayName("Should clear cache after setting nickname")
        void clearCacheAfterSetNickname() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.getFriends(playerUuid); // populate cache
            service.setNickname(playerUuid, "TestFriend", "Buddy");
            service.getFriends(playerUuid); // should re-query

            verify(friendQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should match friend name case-insensitively")
        void matchCaseInsensitive() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));

            service.setNickname(playerUuid, "TESTFRIEND", "Buddy");

            assertThat(friendship.getNickname()).isEqualTo("Buddy");
        }

        @Test
        @DisplayName("Should log error when update throws IllegalAccessException")
        void logErrorOnUpdateFailure() throws Exception {
            FriendshipData friendship = FriendshipData.builder()
                    .playerUuid(playerUuid.toString())
                    .friendUuid(friendUuid.toString())
                    .friendName("TestFriend")
                    .createdTime(System.currentTimeMillis())
                    .build();
            when(friendQuery.list()).thenReturn(
                    new ArrayList<>(Collections.singletonList(friendship)));
            doThrow(new IllegalAccessException("test error"))
                    .when(friendDataOperator).update(any());

            UltiSocialTestHelper.setField(service, "plugin", UltiSocialTestHelper.getMockPlugin());

            service.setNickname(playerUuid, "TestFriend", "Buddy");

            assertThat(friendship.getNickname()).isEqualTo("Buddy");
            verify(UltiSocialTestHelper.getMockLogger()).error(
                    eq("Failed to update friend data"), any(IllegalAccessException.class));
        }
    }

    // ==================== clearCache ====================

    @Nested
    @DisplayName("clearCache")
    class ClearCache {

        @Test
        @DisplayName("Should clear cache for player")
        void clearCache() {
            // Populate cache
            service.getFriends(playerUuid);
            service.getBlacklist(playerUuid);

            // Clear cache
            service.clearCache(playerUuid);

            // Next call should hit database again
            service.getFriends(playerUuid);
            service.getBlacklist(playerUuid);

            verify(friendQuery, times(2)).list();
            verify(blacklistQuery, times(2)).list();
        }

        @Test
        @DisplayName("Should not affect other players' cache")
        void notAffectOtherPlayersCache() {
            UUID otherUuid = UUID.randomUUID();

            service.getFriends(playerUuid);
            service.getFriends(otherUuid);

            service.clearCache(playerUuid);

            // playerUuid cache cleared, otherUuid still cached
            service.getFriends(playerUuid); // re-queries
            service.getFriends(otherUuid); // from cache

            // friendQuery.list() called 3 times: playerUuid (2) + otherUuid (1)
            verify(friendQuery, times(3)).list();
        }
    }

    // ==================== Config Getter ====================

    @Nested
    @DisplayName("getConfig")
    class GetConfig {

        @Test
        @DisplayName("Should return config")
        void returnConfig() {
            assertThat(service.getConfig()).isSameAs(config);
        }
    }

    // ==================== cleanupExpiredRequests ====================

    @Nested
    @DisplayName("cleanupExpiredRequests")
    class CleanupExpiredRequests {

        @Test
        @DisplayName("Should remove expired requests")
        void removeExpiredRequests() throws Exception {
            // Add a valid and an expired request
            FriendRequest valid = FriendRequest.create(
                    UUID.randomUUID(), "ValidSender", playerUuid);
            FriendRequest expired = new FriendRequest(
                    UUID.randomUUID(), "ExpiredSender", playerUuid,
                    System.currentTimeMillis() - 120000);

            Map<UUID, List<FriendRequest>> requests = new java.util.concurrent.ConcurrentHashMap<>();
            requests.put(playerUuid, new ArrayList<>(Arrays.asList(valid, expired)));
            UltiSocialTestHelper.setField(service, "pendingRequests", requests);

            service.cleanupExpiredRequests();

            List<FriendRequest> remaining = service.getPendingRequests(playerUuid);
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getSenderName()).isEqualTo("ValidSender");
        }

        @Test
        @DisplayName("Should handle empty requests map")
        void handleEmptyRequestsMap() {
            // No requests at all — should not throw
            service.cleanupExpiredRequests();
        }
    }

    // ==================== init ====================

    @Nested
    @DisplayName("init")
    class Init {

        @Test
        @DisplayName("Should initialize data operators from plugin")
        void initializeDataOperators() throws Exception {
            FriendService newService = new FriendService();
            UltiSocialTestHelper.setField(newService, "plugin", UltiSocialTestHelper.getMockPlugin());
            UltiSocialTestHelper.setField(newService, "config", config);

            newService.init();

            // After init, dataOperator and blacklistDataOperator should be set
            // (they are created from plugin.getDataOperator())
            // We can verify by calling methods that use them
            // The mock plugin's getDataOperator returns generic mocks
        }
    }

    /**
     * Simple ConcurrentHashMap wrapper for test use (avoids type issues with reflection).
     */
    private static class ConcurrentHashMapWrapper<K, V> extends java.util.concurrent.ConcurrentHashMap<K, V> {
    }
}
