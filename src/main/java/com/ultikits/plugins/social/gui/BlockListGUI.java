package com.ultikits.plugins.social.gui;

import com.ultikits.plugins.social.entity.BlacklistData;
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
 * Blacklist management GUI.
 *
 * @author wisdomme
 * @version 1.0.0
 */
public class BlockListGUI implements InventoryHolder {
    
    private final FriendService friendService;
    private final Player viewer;
    private final Inventory inventory;
    private final List<BlacklistData> blockedUsers;
    private int currentPage = 0;
    
    private static final int ITEMS_PER_PAGE = 45;
    
    public BlockListGUI(FriendService friendService, Player viewer) {
        this.friendService = friendService;
        this.viewer = viewer;
        this.blockedUsers = new ArrayList<>(friendService.getBlacklist(viewer.getUniqueId()));
        
        String title = i18n("gui_blocklist") + " " + ChatColor.GRAY + "(" + blockedUsers.size() + ")";
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
        int end = Math.min(start + ITEMS_PER_PAGE, blockedUsers.size());
        
        for (int i = start; i < end; i++) {
            BlacklistData blocked = blockedUsers.get(i);
            inventory.setItem(i - start, createBlockedUserItem(blocked));
        }
        
        // Navigation row
        addNavigationRow();
    }
    
    /**
     * Create an item representing a blocked user.
     */
    private ItemStack createBlockedUserItem(BlacklistData blocked) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        if (meta != null) {
            // Set skull owner
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(blocked.getBlockedUuid())));
            
            // Display name
            meta.setDisplayName(ChatColor.RED + "✖ " + ChatColor.WHITE + blocked.getBlockedName());
            
            // Lore
            List<String> lore = new ArrayList<>();
            lore.add(i18n("gui_blocked_time").replace("{TIME}", formatTime(blocked.getCreatedTime())));
            
            if (blocked.getReason() != null && !blocked.getReason().isEmpty()) {
                lore.add(i18n("gui_block_reason").replace("{REASON}", blocked.getReason()));
            }
            
            lore.add("");
            lore.add(i18n("gui_click_unblock"));
            lore.add(i18n("gui_unblock_hint"));
            
            meta.setLore(lore);
            skull.setItemMeta(meta);
        }
        
        return skull;
    }
    
    /**
     * Add navigation row.
     */
    private void addNavigationRow() {
        int totalPages = (int) Math.ceil((double) blockedUsers.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        // Fill bottom row with glass
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, filler);
        }
        
        // Previous page
        if (currentPage > 0) {
            inventory.setItem(45, createItem(Material.ARROW, i18n("gui_prev_page")));
        }
        
        // Back to friend list button
        inventory.setItem(47, createItem(Material.BOOK, 
            i18n("gui_back"),
            i18n("gui_click_back")));
        
        // Page indicator
        inventory.setItem(49, createItem(Material.PAPER, 
            i18n("gui_page").replace("{PAGE}", String.valueOf(currentPage + 1))
                .replace("{TOTAL}", String.valueOf(totalPages)),
            i18n("gui_blocked_total").replace("{COUNT}", String.valueOf(blockedUsers.size()))));
        
        // Empty slot info
        if (blockedUsers.isEmpty()) {
            inventory.setItem(22, createItem(Material.EMERALD,
                i18n("gui_blocklist_empty"),
                i18n("gui_blocklist_empty_hint")));
        }
        
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
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    /**
     * Get blocked user at slot.
     */
    public BlacklistData getBlockedUserAtSlot(int slot) {
        if (slot < 0 || slot >= ITEMS_PER_PAGE) return null;
        
        int index = currentPage * ITEMS_PER_PAGE + slot;
        if (index >= blockedUsers.size()) return null;
        
        return blockedUsers.get(index);
    }
    
    /**
     * Go to next page.
     */
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) blockedUsers.size() / ITEMS_PER_PAGE);
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
     * Refresh blacklist.
     */
    public void refresh() {
        friendService.clearCache(viewer.getUniqueId());
        blockedUsers.clear();
        blockedUsers.addAll(friendService.getBlacklist(viewer.getUniqueId()));
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
