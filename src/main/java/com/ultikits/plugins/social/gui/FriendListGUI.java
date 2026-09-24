package com.ultikits.plugins.social.gui;

import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.service.FriendService;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Friend list GUI.
 *
 * @author wisdomme
 * @version 1.0.0
 */
public class FriendListGUI implements InventoryHolder {
    
    private final FriendService friendService;
    private final Player viewer;
    private final Inventory inventory;
    private final List<FriendshipData> friends;
    private int currentPage = 0;
    
    private static final int ITEMS_PER_PAGE = 45;
    
    public FriendListGUI(FriendService friendService, Player viewer) {
        this.friendService = friendService;
        this.viewer = viewer;
        this.friends = friendService.getFriends(viewer.getUniqueId());
        
        String title = FriendService.configuredOr(friendService.getConfig().getGuiTitle(),
                friendService.i18n("gui_friend_list"))
            .replace("{COUNT}", String.valueOf(friends.size()))
            .replace("{MAX}", String.valueOf(friendService.getConfig().getMaxFriends()))
            .replace("&", "§");
        
        this.inventory = Bukkit.createInventory(this, 54, title);
        updateInventory();
    }
    
    /** This module's language-file text for {@code key}, with its {@code &} colour codes applied. */
    private String i18n(String key) {
        return ChatColor.translateAlternateColorCodes('&', friendService.i18n(key));
    }

    /**
     * Update inventory contents.
     */
    public void updateInventory() {
        inventory.clear();
        
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, friends.size());
        
        for (int i = start; i < end; i++) {
            FriendshipData friend = friends.get(i);
            inventory.setItem(i - start, createFriendItem(friend));
        }
        
        // Navigation row
        addNavigationRow();
    }
    
    /**
     * Create an item representing a friend.
     */
    private ItemStack createFriendItem(FriendshipData friend) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        if (meta != null) {
            // Set skull owner
            Player onlineFriend = Bukkit.getPlayer(UUID.fromString(friend.getFriendUuid()));
            boolean online = onlineFriend != null;
            
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(friend.getFriendUuid())));
            
            // Display name
            String displayName = friend.getNickname() != null ? 
                friend.getNickname() + " §7(" + friend.getFriendName() + ")" :
                friend.getFriendName();
            
            if (friend.isFavorite()) {
                displayName = "§e★ " + displayName;
            }
            
            meta.setDisplayName((online ? ChatColor.GREEN : ChatColor.GRAY) + displayName);
            
            // Lore
            List<String> lore = new ArrayList<>();
            lore.add(online ? i18n("status_online") : i18n("status_offline"));
            
            if (online && onlineFriend != null) {
                lore.add(i18n("gui_world").replace("{WORLD}", onlineFriend.getWorld().getName()));
                // Show game mode
                String gameMode = onlineFriend.getGameMode().name();
                String gameModeDisplay = formatGameMode(gameMode);
                lore.add(i18n("gui_mode").replace("{MODE}", gameModeDisplay));
            }
            
            lore.add(i18n("gui_added_time").replace("{TIME}", formatTime(friend.getCreatedTime())));
            lore.add("");
            
            if (online && friendService.getConfig().isTpToFriendEnabled()) {
                lore.add(i18n("gui_click_tp"));
            }
            if (online) {
                lore.add(i18n("gui_click_msg"));
            } else {
                lore.add(i18n("gui_click_remove_offline"));
            }
            lore.add(friend.isFavorite() ? i18n("gui_click_unfavorite") : i18n("gui_click_favorite"));
            lore.add(i18n("gui_click_remove"));
            
            meta.setLore(lore);
            skull.setItemMeta(meta);
        }
        
        return skull;
    }
    
    /**
     * Format game mode for display.
     */
    private String formatGameMode(String gameMode) {
        switch (gameMode.toUpperCase()) {
            case "SURVIVAL": return friendService.i18n("gamemode_survival");
            case "CREATIVE": return friendService.i18n("gamemode_creative");
            case "ADVENTURE": return friendService.i18n("gamemode_adventure");
            case "SPECTATOR": return friendService.i18n("gamemode_spectator");
            default: return gameMode;
        }
    }
    
    /**
     * Add navigation row.
     */
    private void addNavigationRow() {
        int totalPages = (int) Math.ceil((double) friends.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        // Fill bottom row with glass
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, filler);
        }
        
        // Previous page
        if (currentPage > 0) {
            inventory.setItem(45, createItem(Material.ARROW, i18n("gui_prev_page")));
        }
        
        // Pending requests button
        int requestCount = friendService.getPendingRequests(viewer.getUniqueId()).size();
        if (requestCount > 0) {
            inventory.setItem(47, createItem(Material.WRITABLE_BOOK, 
                i18n("gui_requests").replace("{COUNT}", String.valueOf(requestCount)),
                i18n("gui_click_view")));
        }
        
        // Page indicator
        inventory.setItem(49, createItem(Material.BOOK, 
            i18n("gui_page").replace("{PAGE}", String.valueOf(currentPage + 1))
                .replace("{TOTAL}", String.valueOf(totalPages))));
        
        // Next page
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createItem(Material.ARROW, i18n("gui_next_page")));
        }
    }
    
    /**
     * Create an item with name and lore.
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Format timestamp.
     */
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    /**
     * Get friend at slot.
     */
    public FriendshipData getFriendAtSlot(int slot) {
        if (slot < 0 || slot >= ITEMS_PER_PAGE) return null;
        
        int index = currentPage * ITEMS_PER_PAGE + slot;
        if (index >= friends.size()) return null;
        
        return friends.get(index);
    }
    
    /**
     * Go to next page.
     */
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) friends.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateInventory();
        }
    }
    
    /**
     * Go to previous page.
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateInventory();
        }
    }
    
    /**
     * Refresh friends list.
     */
    public void refresh() {
        friendService.clearCache(viewer.getUniqueId());
        friends.clear();
        friends.addAll(friendService.getFriends(viewer.getUniqueId()));
        updateInventory();
    }
    
    public Player getViewer() {
        return viewer;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
