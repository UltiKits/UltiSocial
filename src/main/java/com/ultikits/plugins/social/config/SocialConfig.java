package com.ultikits.plugins.social.config;

import com.ultikits.ultitools.abstracts.AbstractConfigEntity;
import com.ultikits.ultitools.annotations.ConfigEntity;
import com.ultikits.ultitools.annotations.ConfigEntry;
import com.ultikits.ultitools.annotations.config.Range;

import lombok.Getter;
import lombok.Setter;

/**
 * Social system configuration.
 *
 * @author wisdomme
 * @version 1.0.0
 */
@Getter
@Setter
@ConfigEntity("config/social.yml")
public class SocialConfig extends AbstractConfigEntity {

    @Range(min = 1, max = 500)
    @ConfigEntry(path = "max_friends", comment = "Maximum number of friends per player")
    private int maxFriends = 50;

    @Range(min = 10, max = 3600)
    @ConfigEntry(path = "request_timeout", comment = "Friend request timeout in seconds")
    private int requestTimeout = 60;

    @ConfigEntry(path = "notifications.friend_online", comment = "Notify when friend comes online")
    private boolean notifyFriendOnline = true;

    @ConfigEntry(path = "notifications.friend_offline", comment = "Notify when friend goes offline")
    private boolean notifyFriendOffline = true;

    @ConfigEntry(path = "tp_to_friend.enabled", comment = "Allow teleporting to friends")
    private boolean tpToFriendEnabled = true;

    @Range(min = 0, max = 3600)
    @ConfigEntry(path = "tp_to_friend.cooldown", comment = "Teleport cooldown in seconds")
    private int tpCooldown = 30;
    
    // The friend-list title and the messages below are blank by default: a blank value shows the
    // language file's text in the server's language, resolved when it is shown (maintainer ruling
    // 2026-09-24 (d)). Any other value is the operator's and is shown as written.

    @ConfigEntry(path = "gui_title", comment = "Friend list GUI title (blank: the language file's text)")
    private String guiTitle = "";

    @ConfigEntry(path = "messages.friend_added", comment = "Friend added message (blank: the language file's text)")
    private String friendAddedMessage = "";

    @ConfigEntry(path = "messages.friend_removed", comment = "Friend removed message (blank: the language file's text)")
    private String friendRemovedMessage = "";

    @ConfigEntry(path = "messages.friend_online", comment = "Friend online notification (blank: the language file's text)")
    private String friendOnlineMessage = "";

    @ConfigEntry(path = "messages.friend_offline", comment = "Friend offline notification (blank: the language file's text)")
    private String friendOfflineMessage = "";

    @ConfigEntry(path = "messages.request_sent", comment = "Request sent message (blank: the language file's text)")
    private String requestSentMessage = "";

    @ConfigEntry(path = "messages.request_received", comment = "Request received message (blank: the language file's text)")
    private String requestReceivedMessage = "";

    @ConfigEntry(path = "messages.request_denied", comment = "Request denied message (blank: the language file's text)")
    private String requestDeniedMessage = "";

    @ConfigEntry(path = "messages.max_friends_reached", comment = "Max friends reached message (blank: the language file's text)")
    private String maxFriendsMessage = "";

    @ConfigEntry(path = "messages.already_friends", comment = "Already friends message (blank: the language file's text)")
    private String alreadyFriendsMessage = "";

    @ConfigEntry(path = "messages.blocked", comment = "Blocked player message (bidirectional) (blank: the language file's text)")
    private String blockedMessage = "";

    /**
     * The default each text setting had in every earlier version, read from this class's history (one
     * value per setting, from the first release until the language file took over). Kept only to be
     * recognised in an upgraded operator's file and blanked; never shown.
     */
    private static final String[][] SHIPPED_DEFAULTS = {
        {"guiTitle", "&6好友列表 &7({COUNT}/{MAX})"},
        {"friendAddedMessage", "&a你和 {PLAYER} 成为了好友！"},
        {"friendRemovedMessage", "&c你已删除好友 {PLAYER}"},
        {"friendOnlineMessage", "&a你的好友 {PLAYER} 上线了！"},
        {"friendOfflineMessage", "&7你的好友 {PLAYER} 下线了"},
        {"requestSentMessage", "&a已向 {PLAYER} 发送好友请求！"},
        {"requestReceivedMessage", "&e{PLAYER} 想和你成为好友！输入 /friend accept {PLAYER} 接受"},
        {"requestDeniedMessage", "&c已拒绝 {PLAYER} 的好友请求"},
        {"maxFriendsMessage", "&c你的好友数量已达上限！"},
        {"alreadyFriendsMessage", "&c你已经和 {PLAYER} 是好友了！"},
        {"blockedMessage", "&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系"}
    };

    /**
     * Blanks every text setting that is exactly the default an earlier version shipped, so the
     * language file's text takes over in the server's language; any other value is the operator's and
     * is kept. Idempotent: a blank value matches no shipped default. The caller saves the file when
     * this returns true (maintainer ruling 2026-09-24 (d)).
     *
     * @return whether any value was rewritten
     */
    public boolean migrateLegacyDefaults() {
        boolean changed = false;
        if (SHIPPED_DEFAULTS[0][1].equals(guiTitle)) {
            guiTitle = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[1][1].equals(friendAddedMessage)) {
            friendAddedMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[2][1].equals(friendRemovedMessage)) {
            friendRemovedMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[3][1].equals(friendOnlineMessage)) {
            friendOnlineMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[4][1].equals(friendOfflineMessage)) {
            friendOfflineMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[5][1].equals(requestSentMessage)) {
            requestSentMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[6][1].equals(requestReceivedMessage)) {
            requestReceivedMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[7][1].equals(requestDeniedMessage)) {
            requestDeniedMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[8][1].equals(maxFriendsMessage)) {
            maxFriendsMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[9][1].equals(alreadyFriendsMessage)) {
            alreadyFriendsMessage = "";
            changed = true;
        }
        if (SHIPPED_DEFAULTS[10][1].equals(blockedMessage)) {
            blockedMessage = "";
            changed = true;
        }
        return changed;
    }

    public SocialConfig(String configFilePath) {
        super(configFilePath);
    }
}
