package com.ultikits.plugins.social.listener;

import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.gui.BlockListGUI;
import com.ultikits.plugins.social.gui.FriendListGUI;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.annotations.Autowired;
import com.ultikits.ultitools.annotations.EventListener;
import com.ultikits.ultitools.services.NotificationService;
import com.ultikits.ultitools.services.TeleportService;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener for social events.
 * Handles player join/quit notifications and GUI interactions.
 *
 * @author wisdomme
 * @version 1.1.0
 */
@EventListener
public class SocialListener implements Listener {
    
    @Autowired
    private FriendService friendService;
    
    @Autowired(required = false)
    private NotificationService notificationService;
    
    @Autowired(required = false)
    private TeleportService teleportService;
    
    /** Catalogue text with its {@code &} colour codes applied. */
    private static String text(String catalogueText) {
        return ChatColor.translateAlternateColorCodes('&', catalogueText);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!friendService.getConfig().isNotifyFriendOnline()) {
            return;
        }
        
        // Notify friends that player is online
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            
            if (friendService.areFriends(online.getUniqueId(), player.getUniqueId())) {
                String message = FriendService.configuredOr(friendService.getConfig().getFriendOnlineMessage(),
                        friendService.i18n("friend_online"))
                    .replace("{PLAYER}", player.getName())
                    .replace("&", "§");
                
                // Use NotificationService if available
                if (notificationService != null) {
                    notificationService.sendMessageNotification(online, message);
                } else {
                    online.sendMessage(message);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear cache
        friendService.clearCache(player.getUniqueId());
        
        if (!friendService.getConfig().isNotifyFriendOffline()) {
            return;
        }
        
        // Notify friends that player is offline
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            
            if (friendService.areFriends(online.getUniqueId(), player.getUniqueId())) {
                String message = FriendService.configuredOr(friendService.getConfig().getFriendOfflineMessage(),
                        friendService.i18n("friend_offline"))
                    .replace("{PLAYER}", player.getName())
                    .replace("&", "§");
                
                // Use NotificationService if available
                if (notificationService != null) {
                    notificationService.sendMessageNotification(online, message);
                } else {
                    online.sendMessage(message);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Handle FriendListGUI
        if (event.getInventory().getHolder() instanceof FriendListGUI) {
            handleFriendListClick(event);
            return;
        }
        
        // Handle BlockListGUI
        if (event.getInventory().getHolder() instanceof BlockListGUI) {
            handleBlockListClick(event);
            return;
        }
    }
    
    /**
     * Handle clicks in FriendListGUI.
     */
    private void handleFriendListClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        FriendListGUI gui = (FriendListGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        // Navigation buttons
        if (slot == 45) { // Previous page
            gui.previousPage();
            return;
        }
        if (slot == 53) { // Next page
            gui.nextPage();
            return;
        }
        if (slot == 47) { // Pending requests
            player.closeInventory();
            player.performCommand("friend requests");
            return;
        }
        
        // Friend item clicks
        if (slot >= 0 && slot < 45) {
            FriendshipData friend = gui.getFriendAtSlot(slot);
            if (friend == null) return;
            
            Player target = Bukkit.getPlayer(UUID.fromString(friend.getFriendUuid()));
            boolean online = target != null;
            
            if (event.isLeftClick()) {
                if (event.isShiftClick()) {
                    // Shift+Left: Toggle favorite
                    friendService.toggleFavorite(player.getUniqueId(), friend.getFriendName());
                    gui.refresh();
                    player.sendMessage(text(friendService.i18n("favorite_updated")));
                } else {
                    // Left: Teleport to friend (if online)
                    if (online && friendService.getConfig().isTpToFriendEnabled()) {
                        if (!friendService.canTeleport(player.getUniqueId())) {
                            int remaining = friendService.getRemainingCooldown(player.getUniqueId());
                            player.sendMessage(text(friendService.i18n("tp_cooldown")).replace("{SECONDS}", String.valueOf(remaining)));
                        } else {
                            player.closeInventory();
                            // Use TeleportService if available
                            if (teleportService != null) {
                                teleportService.teleport(player, target.getLocation());
                            } else {
                                player.teleport(target.getLocation());
                            }
                            friendService.setTpCooldown(player.getUniqueId());
                            player.sendMessage(text(friendService.i18n("tp_success")).replace("{PLAYER}", friend.getFriendName()));
                        }
                    } else if (!online) {
                        player.sendMessage(text(friendService.i18n("friend_not_online")).replace("{PLAYER}", friend.getFriendName()));
                    }
                }
            } else if (event.isRightClick()) {
                if (event.isShiftClick()) {
                    // Shift+Right: Delete friend
                    player.closeInventory();
                    friendService.removeFriend(player, friend.getFriendName());
                } else {
                    // Right: Send message (if online) or delete (if offline)
                    if (online) {
                        player.closeInventory();
                        player.sendMessage(text(friendService.i18n("msg_use_command")).replace("{PLAYER}", friend.getFriendName()));
                    } else {
                        // Offline - delete friend
                        player.closeInventory();
                        friendService.removeFriend(player, friend.getFriendName());
                    }
                }
            }
        }
    }
    
    /**
     * Handle clicks in BlockListGUI.
     */
    private void handleBlockListClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        BlockListGUI gui = (BlockListGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        // Navigation buttons
        if (slot == 45) { // Previous page
            gui.previousPage();
            return;
        }
        if (slot == 53) { // Next page
            gui.nextPage();
            return;
        }
        if (slot == 47) { // Back to friend list
            player.closeInventory();
            FriendListGUI friendGui = new FriendListGUI(friendService, player);
            player.openInventory(friendGui.getInventory());
            return;
        }
        
        // Blocked user clicks
        if (slot >= 0 && slot < 45) {
            BlacklistData blocked = gui.getBlockedUserAtSlot(slot);
            if (blocked == null) return;
            
            if (event.isLeftClick()) {
                // Unblock
                if (friendService.removeFromBlacklist(player, blocked.getBlockedName())) {
                    player.sendMessage(text(friendService.i18n("player_unblocked")).replace("{PLAYER}", blocked.getBlockedName()));
                    gui.refresh();
                } else {
                    player.sendMessage(text(friendService.i18n("unblock_failed")));
                }
            }
        }
    }
}
