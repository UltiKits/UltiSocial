package com.ultikits.plugins.social.commands;

import com.ultikits.plugins.social.entity.BlacklistData;
import com.ultikits.plugins.social.entity.FriendRequest;
import com.ultikits.plugins.social.entity.FriendshipData;
import com.ultikits.plugins.social.gui.BlockListGUI;
import com.ultikits.plugins.social.gui.FriendListGUI;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.abstracts.command.BaseCommandExecutor;
import com.ultikits.ultitools.annotations.command.*;
import com.ultikits.ultitools.services.TeleportService;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Friend command executor.
 * Supports friend management, blacklist, teleport and messaging.
 *
 * @author wisdomme
 * @version 1.1.0
 */
@CmdTarget(CmdTarget.CmdTargetType.PLAYER)
@CmdExecutor(
    alias = {"friend", "friends", "f"},
    permission = "ultisocial.use",
    description = "command_description"
)
public class FriendCommand extends BaseCommandExecutor {
    
    private final FriendService friendService;
    private final TeleportService teleportService;
    
    public FriendCommand(FriendService friendService, TeleportService teleportService) {
        this.friendService = friendService;
        this.teleportService = teleportService;
    }

    /** Catalogue text with its {@code &} colour codes applied. */
    private String text(String catalogueText) {
        return ChatColor.translateAlternateColorCodes('&', catalogueText);
    }
    
    // ==================== Friend Commands ====================
    
    @CmdMapping(format = "")
    public void openFriendList(@CmdSender Player player) {
        FriendListGUI gui = new FriendListGUI(friendService, player);
        player.openInventory(gui.getInventory());
    }
    
    @CmdMapping(format = "list")
    public void listFriends(@CmdSender Player player) {
        List<FriendshipData> friends = friendService.getFriends(player.getUniqueId());
        
        if (friends.isEmpty()) {
            player.sendMessage(text(friendService.i18n("list_empty")));
            return;
        }
        
        player.sendMessage(text(friendService.i18n("list_header").replace("{COUNT}", String.valueOf(friends.size()))));
        for (FriendshipData friend : friends) {
            Player online = Bukkit.getPlayer(UUID.fromString(friend.getFriendUuid()));
            String status = online != null ? text(friendService.i18n("status_online")) : text(friendService.i18n("status_offline"));
            String star = friend.isFavorite() ? ChatColor.YELLOW + "★ " : "";
            player.sendMessage(star + status + " " + ChatColor.WHITE + friend.getFriendName());
        }
    }
    
    @CmdMapping(format = "add <player>")
    public void addFriend(@CmdSender Player sender, @CmdParam("player") String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(text(friendService.i18n("player_not_online")).replace("{PLAYER}", targetName));
            return;
        }
        
        if (target.equals(sender)) {
            sender.sendMessage(text(friendService.i18n("cannot_add_self")));
            return;
        }
        
        friendService.sendRequest(sender, target);
    }
    
    @CmdMapping(format = "accept <player>")
    public void acceptRequest(@CmdSender Player player, @CmdParam("player") String senderName) {
        friendService.acceptRequest(player, senderName);
    }
    
    @CmdMapping(format = "deny <player>")
    public void denyRequest(@CmdSender Player player, @CmdParam("player") String senderName) {
        friendService.denyRequest(player, senderName);
    }
    
    @CmdMapping(format = "remove <player>")
    public void removeFriend(@CmdSender Player player, @CmdParam("player") String friendName) {
        friendService.removeFriend(player, friendName);
    }
    
    @CmdMapping(format = "requests")
    public void viewRequests(@CmdSender Player player) {
        List<FriendRequest> requests = friendService.getPendingRequests(player.getUniqueId());
        
        if (requests.isEmpty()) {
            player.sendMessage(text(friendService.i18n("requests_empty")));
            return;
        }
        
        player.sendMessage(text(friendService.i18n("requests_header")));
        for (FriendRequest request : requests) {
            player.sendMessage(text(friendService.i18n("requests_entry")).replace("{PLAYER}", request.getSenderName()));
        }
    }
    
    // ==================== Teleport Commands ====================
    
    @CmdMapping(format = "tp <player>")
    public void teleportToFriend(@CmdSender Player player, @CmdParam("player") String friendName) {
        if (!friendService.getConfig().isTpToFriendEnabled()) {
            player.sendMessage(text(friendService.i18n("tp_disabled")));
            return;
        }
        
        List<FriendshipData> friends = friendService.getFriends(player.getUniqueId());
        FriendshipData targetFriend = null;
        for (FriendshipData friend : friends) {
            if (friend.getFriendName().equalsIgnoreCase(friendName)) {
                targetFriend = friend;
                break;
            }
        }
        
        if (targetFriend == null) {
            player.sendMessage(text(friendService.i18n("not_friend")).replace("{PLAYER}", friendName));
            return;
        }
        
        Player target = Bukkit.getPlayer(UUID.fromString(targetFriend.getFriendUuid()));
        if (target == null) {
            player.sendMessage(text(friendService.i18n("friend_not_online")).replace("{PLAYER}", friendName));
            return;
        }
        
        if (!friendService.canTeleport(player.getUniqueId())) {
            int remaining = friendService.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(text(friendService.i18n("tp_cooldown")).replace("{SECONDS}", String.valueOf(remaining)));
            return;
        }
        
        // Use TeleportService for teleportation
        if (teleportService != null) {
            teleportService.teleport(player, target.getLocation());
        } else {
            player.teleport(target.getLocation());
        }
        
        friendService.setTpCooldown(player.getUniqueId());
        player.sendMessage(text(friendService.i18n("tp_success")).replace("{PLAYER}", friendName));
    }
    
    // ==================== Message Commands ====================
    
    @CmdMapping(format = "msg <player> <message...>")
    public void sendMessage(@CmdSender Player sender, @CmdParam("player") String friendName, 
                           @CmdParam("message") String[] messageParts) {
        // Check if target is friend
        List<FriendshipData> friends = friendService.getFriends(sender.getUniqueId());
        FriendshipData targetFriend = null;
        for (FriendshipData friend : friends) {
            if (friend.getFriendName().equalsIgnoreCase(friendName)) {
                targetFriend = friend;
                break;
            }
        }
        
        if (targetFriend == null) {
            sender.sendMessage(text(friendService.i18n("msg_only_friend")).replace("{PLAYER}", friendName));
            return;
        }
        
        Player target = Bukkit.getPlayer(UUID.fromString(targetFriend.getFriendUuid()));
        if (target == null) {
            sender.sendMessage(text(friendService.i18n("friend_not_online")).replace("{PLAYER}", friendName));
            return;
        }
        
        String message = String.join(" ", messageParts).trim();

        if (message.isEmpty() || message.chars()
                .allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c))) {
            sender.sendMessage(text(friendService.i18n("msg_empty")));
            return;
        }
        
        // The player's own words are appended after the catalogue text, never passed through it: a
        // colour code or a placeholder-shaped token typed by a player stays exactly as typed.
        String prefix = text(friendService.i18n("msg_prefix")) + " ";

        // Send to target
        target.sendMessage(prefix + text(friendService.i18n("msg_received")).replace("{SENDER}", sender.getName())
            + message);
        
        // Confirm to sender
        sender.sendMessage(prefix + text(friendService.i18n("msg_sent")).replace("{RECEIVER}", target.getName())
            + message);
    }
    
    // ==================== Blacklist Commands ====================
    
    @CmdMapping(format = "block <player>")
    public void blockPlayer(@CmdSender Player player, @CmdParam("player") String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            // Try offline player
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
            if (!offline.hasPlayedBefore()) {
                player.sendMessage(text(friendService.i18n("player_not_exist")).replace("{PLAYER}", targetName));
                return;
            }
            
            if (friendService.addToBlacklist(player.getUniqueId(), offline.getUniqueId(), targetName, null)) {
                player.sendMessage(text(friendService.i18n("player_blocked")).replace("{PLAYER}", targetName));
            } else {
                player.sendMessage(text(friendService.i18n("already_blocked")).replace("{PLAYER}", targetName));
            }
            return;
        }
        
        if (target.equals(player)) {
            player.sendMessage(text(friendService.i18n("cannot_block_self")));
            return;
        }
        
        if (friendService.addToBlacklist(player, target, null)) {
            player.sendMessage(text(friendService.i18n("player_blocked")).replace("{PLAYER}", targetName));
            // Notify if they were friends
            if (friendService.areFriends(player.getUniqueId(), target.getUniqueId())) {
                player.sendMessage(text(friendService.i18n("auto_unfriend")));
            }
        } else {
            player.sendMessage(text(friendService.i18n("already_blocked")).replace("{PLAYER}", targetName));
        }
    }
    
    @CmdMapping(format = "unblock <player>")
    public void unblockPlayer(@CmdSender Player player, @CmdParam("player") String targetName) {
        if (friendService.removeFromBlacklist(player, targetName)) {
            player.sendMessage(text(friendService.i18n("player_unblocked")).replace("{PLAYER}", targetName));
        } else {
            player.sendMessage(text(friendService.i18n("not_in_blocklist")).replace("{PLAYER}", targetName));
        }
    }
    
    @CmdMapping(format = "blocklist")
    public void openBlockList(@CmdSender Player player) {
        BlockListGUI gui = new BlockListGUI(friendService, player);
        player.openInventory(gui.getInventory());
    }
    
    // ==================== Help Command ====================
    
    @CmdMapping(format = "help")
    public void help(@CmdSender Player player) {
        player.sendMessage(text(friendService.i18n("help_title")));
        player.sendMessage(text(friendService.i18n("help_friend")));
        player.sendMessage(text(friendService.i18n("help_list")));
        player.sendMessage(text(friendService.i18n("help_add")));
        player.sendMessage(text(friendService.i18n("help_accept")));
        player.sendMessage(text(friendService.i18n("help_deny")));
        player.sendMessage(text(friendService.i18n("help_remove")));
        player.sendMessage(text(friendService.i18n("help_tp")));
        player.sendMessage(text(friendService.i18n("help_msg")));
        player.sendMessage(text(friendService.i18n("help_requests")));
        player.sendMessage(text(friendService.i18n("help_block_title")));
        player.sendMessage(text(friendService.i18n("help_block")));
        player.sendMessage(text(friendService.i18n("help_unblock")));
        player.sendMessage(text(friendService.i18n("help_blocklist")));
    }
    
    @Override
    protected void handleHelp(CommandSender sender) {
        if (sender instanceof Player) {
            help((Player) sender);
        }
    }
    
    // ==================== Tab Complete ====================
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Let parent handle basic completion first
        List<String> parentSuggestions = super.onTabComplete(sender, command, alias, args);
        if (parentSuggestions != null && !parentSuggestions.isEmpty()) {
            return parentSuggestions;
        }
        
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            suggestions.add("list");
            suggestions.add("add");
            suggestions.add("accept");
            suggestions.add("deny");
            suggestions.add("remove");
            suggestions.add("tp");
            suggestions.add("msg");
            suggestions.add("requests");
            suggestions.add("block");
            suggestions.add("unblock");
            suggestions.add("blocklist");
            suggestions.add("help");
            
            return filterStartsWith(suggestions, args[0]);
        }
        
        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            
            switch (subCmd) {
                case "add":
                case "block":
                    // Online players (excluding self and already friends/blocked)
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.equals(player)) {
                            suggestions.add(online.getName());
                        }
                    }
                    break;
                    
                case "accept":
                case "deny":
                    // Pending request senders
                    for (FriendRequest req : friendService.getPendingRequests(player.getUniqueId())) {
                        suggestions.add(req.getSenderName());
                    }
                    break;
                    
                case "remove":
                case "tp":
                case "msg":
                    // Friends list
                    for (FriendshipData friend : friendService.getFriends(player.getUniqueId())) {
                        suggestions.add(friend.getFriendName());
                    }
                    break;
                    
                case "unblock":
                    // Blocked users
                    for (BlacklistData blocked : friendService.getBlacklist(player.getUniqueId())) {
                        suggestions.add(blocked.getBlockedName());
                    }
                    break;
                default:
                    // No suggestions for unknown subcommands
                    break;
            }

            return filterStartsWith(suggestions, args[1]);
        }
        
        return suggestions;
    }
    
    /**
     * Filter suggestions that start with given prefix.
     */
    private List<String> filterStartsWith(List<String> suggestions, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return suggestions;
        }
        String lowerPrefix = prefix.toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
            .collect(Collectors.toList());
    }
}
