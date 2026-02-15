package com.ultikits.plugins.social.gui;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.service.FriendService;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for BlockListGUI.
 */
@DisplayName("BlockListGUI Tests")
class BlockListGUITest {

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
        @DisplayName("Should create GUI with empty blocklist")
        void createWithEmptyList() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
                assertThat(gui.getInventory()).isEqualTo(mockInventory);
                // Should show empty blocklist item at slot 22
                verify(mockInventory, atLeastOnce()).setItem(eq(22), any());
            }
        }

        @Test
        @DisplayName("Should create GUI with blocked users")
        void createWithBlockedUsers() {
            UUID blockedUuid = UUID.randomUUID();
            BlacklistData blocked = BlacklistData.builder()
                    .playerUuid(viewerUuid.toString())
                    .blockedUuid(blockedUuid.toString())
                    .blockedName("BadPlayer")
                    .createdTime(System.currentTimeMillis())
                    .reason("Spamming")
                    .build();

            when(friendService.getBlacklist(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
                verify(mockInventory, atLeastOnce()).setItem(anyInt(), any());
            }
        }

        @Test
        @DisplayName("Should create GUI with blocked user without reason")
        void createWithBlockedUserNoReason() {
            UUID blockedUuid = UUID.randomUUID();
            BlacklistData blocked = BlacklistData.builder()
                    .playerUuid(viewerUuid.toString())
                    .blockedUuid(blockedUuid.toString())
                    .blockedName("BlockedGuy")
                    .createdTime(System.currentTimeMillis())
                    .reason(null)
                    .build();

            when(friendService.getBlacklist(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }

        @Test
        @DisplayName("Should create GUI with blocked user with empty reason")
        void createWithBlockedUserEmptyReason() {
            UUID blockedUuid = UUID.randomUUID();
            BlacklistData blocked = BlacklistData.builder()
                    .playerUuid(viewerUuid.toString())
                    .blockedUuid(blockedUuid.toString())
                    .blockedName("BlockedGuy")
                    .createdTime(System.currentTimeMillis())
                    .reason("")
                    .build();

            when(friendService.getBlacklist(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getViewer()).isEqualTo(viewer);
            }
        }
    }

    // ==================== getBlockedUserAtSlot ====================

    @Nested
    @DisplayName("getBlockedUserAtSlot")
    class GetBlockedUserAtSlot {

        @Test
        @DisplayName("Should return blocked user at valid slot")
        void returnBlockedUserAtValidSlot() {
            UUID blockedUuid = UUID.randomUUID();
            BlacklistData blocked = BlacklistData.builder()
                    .playerUuid(viewerUuid.toString())
                    .blockedUuid(blockedUuid.toString())
                    .blockedName("Blocked1")
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getBlacklist(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                BlacklistData result = gui.getBlockedUserAtSlot(0);
                assertThat(result).isNotNull();
                assertThat(result.getBlockedName()).isEqualTo("Blocked1");
            }
        }

        @Test
        @DisplayName("Should return null for negative slot")
        void returnNullForNegativeSlot() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getBlockedUserAtSlot(-1)).isNull();
            }
        }

        @Test
        @DisplayName("Should return null for slot >= ITEMS_PER_PAGE")
        void returnNullForSlotBeyondPage() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getBlockedUserAtSlot(45)).isNull();
            }
        }

        @Test
        @DisplayName("Should return null for slot beyond list size")
        void returnNullForSlotBeyondList() {
            BlacklistData blocked = BlacklistData.builder()
                    .playerUuid(viewerUuid.toString())
                    .blockedUuid(UUID.randomUUID().toString())
                    .blockedName("OnlyBlocked")
                    .createdTime(System.currentTimeMillis())
                    .build();

            when(friendService.getBlacklist(viewerUuid)).thenReturn(
                    new ArrayList<>(Collections.singletonList(blocked)));

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                assertThat(gui.getBlockedUserAtSlot(1)).isNull();
            }
        }
    }

    // ==================== Pagination ====================

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        @Test
        @DisplayName("nextPage should not advance when on last page")
        void nextPageOnLastPage() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                gui.nextPage();

                // Only one clear call from constructor
                verify(mockInventory, times(1)).clear();
            }
        }

        @Test
        @DisplayName("previousPage should not go below page 0")
        void previousPageBelowZero() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                gui.previousPage();

                verify(mockInventory, times(1)).clear();
            }
        }

        @Test
        @DisplayName("Should paginate with many blocked users")
        void paginateWithManyBlocked() {
            List<BlacklistData> manyBlocked = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                manyBlocked.add(BlacklistData.builder()
                        .playerUuid(viewerUuid.toString())
                        .blockedUuid(UUID.randomUUID().toString())
                        .blockedName("Blocked" + i)
                        .createdTime(System.currentTimeMillis())
                        .build());
            }

            when(friendService.getBlacklist(viewerUuid)).thenReturn(manyBlocked);

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        SkullMeta skullMeta = mock(SkullMeta.class);
                        when(mock.getItemMeta()).thenReturn(skullMeta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                gui.nextPage();
                verify(mockInventory, times(2)).clear();

                gui.previousPage();
                verify(mockInventory, times(3)).clear();
            }
        }
    }

    // ==================== Refresh ====================

    @Nested
    @DisplayName("Refresh")
    class RefreshTests {

        @Test
        @DisplayName("Should refresh blacklist")
        void refreshBlacklist() {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                gui.refresh();

                verify(friendService).clearCache(viewerUuid);
                verify(friendService, times(2)).getBlacklist(viewerUuid);
            }
        }
    }

    // ==================== formatTime (via reflection) ====================

    @Nested
    @DisplayName("formatTime")
    class FormatTime {

        @Test
        @DisplayName("Should format timestamp with time")
        void formatTimestamp() throws Exception {
            when(friendService.getBlacklist(viewerUuid)).thenReturn(new ArrayList<>());

            try (MockedConstruction<ItemStack> itemMock = mockConstruction(ItemStack.class,
                    (mock, context) -> {
                        ItemMeta meta = mock(ItemMeta.class);
                        when(mock.getItemMeta()).thenReturn(meta);
                    })) {

                BlockListGUI gui = new BlockListGUI(friendService, viewer);

                java.lang.reflect.Method method = BlockListGUI.class.getDeclaredMethod(
                        "formatTime", long.class);
                method.setAccessible(true);

                String result = (String) method.invoke(gui, 0L);
                // Format is yyyy-MM-dd HH:mm
                assertThat(result).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
            }
        }
    }
}
