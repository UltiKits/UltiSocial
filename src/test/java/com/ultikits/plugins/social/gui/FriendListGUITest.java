package com.ultikits.plugins.social.gui;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.FriendRequest;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.service.FriendService;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FriendListGUI.
 */
@DisplayName("FriendListGUI Tests")
class FriendListGUITest {

    private FriendService friendService;
    private SocialConfig config;
    private Player viewer;
    private UUID viewerUuid;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();

        friendService = mock(FriendService.class);
        config = UltiSocialTestHelper.createDefaultConfig();
        when(friendService.getConfig()).thenReturn(config);

        viewerUuid = UUID.randomUUID();
        viewer = UltiSocialTestHelper.createMockPlayer("Viewer", viewerUuid);

        // Mock Bukkit.createInventory to return a mock inventory
        mockInventory = mock(Inventory.class);
        when(UltiSocialTestHelper.getMockServer().createInventory(
                any(), anyInt(), anyString())).thenReturn(mockInventory);
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    // ==================== Constructor ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should create GUI with empty friends list")
        void createWithEmptyList() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
                assertThat(gui.getInventory()).isEqualTo(mockInventory);
            }
        }

        @Test
        @DisplayName("Should create GUI with friends")
        void createWithFriends() {
            UUID friendUuid = UUID.randomUUID();
            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("FriendPlayer")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(null);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
                // Should have set items in inventory
                verify(mockInventory, atLeastOnce()).setItem(anyInt(), any());
            }
        }

        @Test
        @DisplayName("Should create GUI with online friend showing details")
        void createWithOnlineFriend() {
            UUID friendUuid = UUID.randomUUID();
            Player onlineFriend = UltiSocialTestHelper.createMockPlayer("OnlinePal", friendUuid);
            when(onlineFriend.getGameMode()).thenReturn(GameMode.SURVIVAL);

            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("OnlinePal")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(onlineFriend);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }

        @Test
        @DisplayName("Should create GUI with favorite friend showing star")
        void createWithFavoriteFriend() {
            UUID friendUuid = UUID.randomUUID();
            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("FavFriend")
                    .favorite(true)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(null);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }

        @Test
        @DisplayName("Should create GUI with friend having nickname")
        void createWithNicknamedFriend() {
            UUID friendUuid = UUID.randomUUID();
            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("RealName")
                    .nickname("Buddy")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(null);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }

        @Test
        @DisplayName("Should show pending requests button when requests exist")
        void showPendingRequestsButton() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            FriendRequest request = FriendRequest.create(UUID.randomUUID(), "Requester", viewerUuid);
            when(friendService.getPendingRequests(viewerUuid))
                    .thenReturn(Collections.singletonList(request));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                // Slot 47 should have the pending requests button
                verify(mockInventory, atLeastOnce()).setItem(eq(47), any());
            }
        }

        @Test
        @DisplayName("Should create GUI with tp disabled showing no tp lore")
        void createWithTpDisabled() {
            UUID friendUuid = UUID.randomUUID();
            Player onlineFriend = UltiSocialTestHelper.createMockPlayer("OnlinePal", friendUuid);
            when(onlineFriend.getGameMode()).thenReturn(GameMode.CREATIVE);

            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("OnlinePal")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());
            when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(onlineFriend);
            when(config.isTpToFriendEnabled()).thenReturn(false);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }
    }

    // ==================== getFriendAtSlot ====================

    @Nested
    @DisplayName("getFriendAtSlot")
    class GetFriendAtSlot {

        @Test
        @DisplayName("Should return friend at valid slot")
        void returnFriendAtValidSlot() {
            UUID friendUuid = UUID.randomUUID();
            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(friendUuid.toString())
                    .friendName("Friend1")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                FriendshipData result = gui.getFriendAtSlot(0);
                assertThat(result).isNotNull();
                assertThat(result.getFriendName()).isEqualTo("Friend1");
            }
        }

        @Test
        @DisplayName("Should return null for negative slot")
        void returnNullForNegativeSlot() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getFriendAtSlot(-1)).isNull();
            }
        }

        @Test
        @DisplayName("Should return null for slot beyond items per page")
        void returnNullForSlotBeyondPage() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                assertThat(gui.getFriendAtSlot(45)).isNull();
            }
        }

        @Test
        @DisplayName("Should return null for slot beyond friends list")
        void returnNullForSlotBeyondList() {
            FriendshipData friend = FriendshipData.builder()
                    .friendUuid(UUID.randomUUID().toString())
                    .friendName("OnlyFriend")
                    .favorite(false)
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getFriends(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(friend)));
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                // Slot 1 has no friend (only 1 friend at slot 0)
                assertThat(gui.getFriendAtSlot(1)).isNull();
            }
        }
    }

    // ==================== Pagination ====================

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        @Test
        @DisplayName("nextPage should not advance beyond last page")
        void nextPageBeyondLast() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                // With 0 friends, page shouldn't advance
                gui.nextPage();

                // Inventory clear should only be called once (from constructor)
                verify(mockInventory, times(1)).clear();
            }
        }

        @Test
        @DisplayName("previousPage should not go below page 0")
        void previousPageBelowZero() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                // Already at page 0, shouldn't go lower
                gui.previousPage();

                // Inventory clear should only be called once (from constructor)
                verify(mockInventory, times(1)).clear();
            }
        }

        @Test
        @DisplayName("Should paginate with many friends")
        void paginateWithManyFriends() {
            // Create 50 friends to force 2 pages (ITEMS_PER_PAGE = 45)
            List<FriendshipData> manyFriends = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                UUID friendUuid = UUID.randomUUID();
                manyFriends.add(FriendshipData.builder()
                        .friendUuid(friendUuid.toString())
                        .friendName("Friend" + i)
                        .favorite(false)
                        .createdTime(System.currentTimeMillis())
                        .build());
                when(UltiSocialTestHelper.getMockServer().getPlayer(friendUuid)).thenReturn(null);
            }

            when(friendService.getFriends(viewerUuid)).thenReturn(manyFriends);
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                // Next page should work
                gui.nextPage();
                // Inventory should be cleared twice (once for constructor, once for next page)
                verify(mockInventory, times(2)).clear();

                // Previous page should work now
                gui.previousPage();
                verify(mockInventory, times(3)).clear();
            }
        }
    }

    // ==================== Refresh ====================

    @Nested
    @DisplayName("Refresh")
    class Refresh {

        @Test
        @DisplayName("Should refresh friends list")
        void refreshFriendsList() {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                gui.refresh();

                verify(friendService).clearCache(viewerUuid);
                // getFriends called twice: once in constructor, once in refresh
                verify(friendService, times(2)).getFriends(viewerUuid);
            }
        }
    }

    // ==================== formatGameMode (via reflection) ====================

    @Nested
    @DisplayName("formatGameMode")
    class FormatGameMode {

        @Test
        @DisplayName("Should format SURVIVAL")
        void formatSurvival() throws Exception {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                java.lang.reflect.Method method = FriendListGUI.class.getDeclaredMethod(
                        "formatGameMode", String.class);
                method.setAccessible(true);

                assertThat(method.invoke(gui, "SURVIVAL")).isEqualTo("生存模式");
                assertThat(method.invoke(gui, "CREATIVE")).isEqualTo("创造模式");
                assertThat(method.invoke(gui, "ADVENTURE")).isEqualTo("冒险模式");
                assertThat(method.invoke(gui, "SPECTATOR")).isEqualTo("旁观模式");
                assertThat(method.invoke(gui, "UNKNOWN")).isEqualTo("UNKNOWN");
            }
        }
    }

    // ==================== formatTime (via reflection) ====================

    @Nested
    @DisplayName("formatTime")
    class FormatTime {

        @Test
        @DisplayName("Should format timestamp to yyyy-MM-dd")
        void formatTimestamp() throws Exception {
            when(friendService.getFriends(viewerUuid)).thenReturn(new ArrayList<>());
            when(friendService.getPendingRequests(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                FriendListGUI gui = new FriendListGUI(friendService, viewer);

                java.lang.reflect.Method method = FriendListGUI.class.getDeclaredMethod(
                        "formatTime", long.class);
                method.setAccessible(true);

                // Test with a known timestamp
                String result = (String) method.invoke(gui, 0L);
                assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}");
            }
        }
    }
}
