package com.ultikits.plugins.social.config;

import com.ultikits.ultitools.abstracts.AbstractConfigEntity;
import com.ultikits.ultitools.annotations.ConfigEntity;
import com.ultikits.ultitools.annotations.ConfigEntry;
import com.ultikits.ultitools.annotations.config.NotEmpty;
import com.ultikits.ultitools.annotations.config.Range;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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
    
    // The default each text setting had in every earlier version, read from this class's history (one
    // value per setting). Each is the field's Java default, which the framework writes for a missing key,
    // and one of the values materializeText() recognises as built-in text in an operator's file,
    // compared byte for byte.
    private static final String SHIPPED_GUI_TITLE = "&6好友列表 &7({COUNT}/{MAX})";
    private static final String SHIPPED_FRIEND_ADDED_MESSAGE = "&a你和 {PLAYER} 成为了好友！";
    private static final String SHIPPED_FRIEND_REMOVED_MESSAGE = "&c你已删除好友 {PLAYER}";
    private static final String SHIPPED_FRIEND_ONLINE_MESSAGE = "&a你的好友 {PLAYER} 上线了！";
    private static final String SHIPPED_FRIEND_OFFLINE_MESSAGE = "&7你的好友 {PLAYER} 下线了";
    private static final String SHIPPED_REQUEST_SENT_MESSAGE = "&a已向 {PLAYER} 发送好友请求！";
    private static final String SHIPPED_REQUEST_RECEIVED_MESSAGE = "&e{PLAYER} 想和你成为好友！输入 /friend accept {PLAYER} 接受";
    private static final String SHIPPED_REQUEST_DENIED_MESSAGE = "&c已拒绝 {PLAYER} 的好友请求";
    private static final String SHIPPED_MAX_FRIENDS_MESSAGE = "&c你的好友数量已达上限！";
    private static final String SHIPPED_ALREADY_FRIENDS_MESSAGE = "&c你已经和 {PLAYER} 是好友了！";
    private static final String SHIPPED_BLOCKED_MESSAGE = "&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系";

    // Each text setting holds the text the module shows. materializeText() writes it in the server's
    // language while the value is still built-in text (maintainer decision 2026-09-25).

    @NotEmpty
    @ConfigEntry(path = "gui_title", comment = "Friend list GUI title")
    private String guiTitle = SHIPPED_GUI_TITLE;

    @NotEmpty
    @ConfigEntry(path = "messages.friend_added", comment = "Friend added message")
    private String friendAddedMessage = SHIPPED_FRIEND_ADDED_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.friend_removed", comment = "Friend removed message")
    private String friendRemovedMessage = SHIPPED_FRIEND_REMOVED_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.friend_online", comment = "Friend online notification")
    private String friendOnlineMessage = SHIPPED_FRIEND_ONLINE_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.friend_offline", comment = "Friend offline notification")
    private String friendOfflineMessage = SHIPPED_FRIEND_OFFLINE_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.request_sent", comment = "Request sent message")
    private String requestSentMessage = SHIPPED_REQUEST_SENT_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.request_received", comment = "Request received message")
    private String requestReceivedMessage = SHIPPED_REQUEST_RECEIVED_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.request_denied", comment = "Request denied message")
    private String requestDeniedMessage = SHIPPED_REQUEST_DENIED_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.max_friends_reached", comment = "Max friends reached message")
    private String maxFriendsMessage = SHIPPED_MAX_FRIENDS_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.already_friends", comment = "Already friends message")
    private String alreadyFriendsMessage = SHIPPED_ALREADY_FRIENDS_MESSAGE;

    @NotEmpty
    @ConfigEntry(path = "messages.blocked", comment = "Blocked player message (bidirectional)")
    private String blockedMessage = SHIPPED_BLOCKED_MESSAGE;

    /**
     * Writes the friend-list title and every friend message in the server's language (maintainer
     * decision 2026-09-25, UltiKits/UltiSocial#14): each setting whose value is still built-in text --
     * the default an earlier version shipped, or this jar's text for it in any language -- and differs
     * from the current text is replaced with {@code text}'s current text, when that text fits the
     * setting's own limits. Any other value is the operator's and is kept. Idempotent. Must run after the
     * module's language is loaded ({@code registerSelf()} and {@code onReload()}), never from a change
     * listener; the caller saves the file when this returns {@code true}.
     *
     * @param text catalogue key to text in the server's language, from this jar's own catalogue
     *             ({@code ConfigTextDefaults#jarLanguage}), so every value written is in the tracked set
     * @return whether any value was rewritten
     */
    public boolean materializeText(Function<String, String> text) {
        Map<String, Map<String, String>> jar = ConfigTextDefaults.jarCatalogues(SocialConfig.class);
        boolean[] changed = {false};
        guiTitle = follow("guiTitle", guiTitle, text, jar, "gui_friend_list", SHIPPED_GUI_TITLE, changed);
        friendAddedMessage = follow("friendAddedMessage", friendAddedMessage, text, jar, "friend_added", SHIPPED_FRIEND_ADDED_MESSAGE, changed);
        friendRemovedMessage = follow("friendRemovedMessage", friendRemovedMessage, text, jar, "friend_removed", SHIPPED_FRIEND_REMOVED_MESSAGE, changed);
        friendOnlineMessage = follow("friendOnlineMessage", friendOnlineMessage, text, jar, "friend_online", SHIPPED_FRIEND_ONLINE_MESSAGE, changed);
        friendOfflineMessage = follow("friendOfflineMessage", friendOfflineMessage, text, jar, "friend_offline", SHIPPED_FRIEND_OFFLINE_MESSAGE, changed);
        requestSentMessage = follow("requestSentMessage", requestSentMessage, text, jar, "request_sent", SHIPPED_REQUEST_SENT_MESSAGE, changed);
        requestReceivedMessage = follow("requestReceivedMessage", requestReceivedMessage, text, jar, "request_received", SHIPPED_REQUEST_RECEIVED_MESSAGE, changed);
        requestDeniedMessage = follow("requestDeniedMessage", requestDeniedMessage, text, jar, "request_denied", SHIPPED_REQUEST_DENIED_MESSAGE, changed);
        maxFriendsMessage = follow("maxFriendsMessage", maxFriendsMessage, text, jar, "max_friends_reached", SHIPPED_MAX_FRIENDS_MESSAGE, changed);
        alreadyFriendsMessage = follow("alreadyFriendsMessage", alreadyFriendsMessage, text, jar, "already_friends", SHIPPED_ALREADY_FRIENDS_MESSAGE, changed);
        blockedMessage = follow("blockedMessage", blockedMessage, text, jar, "blocked", SHIPPED_BLOCKED_MESSAGE, changed);
        return changed[0];
    }

    /**
     * {@code value}, or {@code text}'s current text for {@code key} when {@code value} is still built-in
     * text other than that and the new text fits {@code field}'s constraints; sets {@code changed[0]}
     * when it replaces.
     */
    private static String follow(String field, String value, Function<String, String> text,
                                 Map<String, Map<String, String>> jar, String key, String shipped, boolean[] changed) {
        String result = ConfigTextDefaults.materialize(SocialConfig.class, field, value,
                ConfigTextDefaults.currentText(text, "", key), ConfigTextDefaults.tracked(jar, "", key, shipped));
        if (!Objects.equals(result, value)) {
            changed[0] = true;
        }
        return result;
    }

    public SocialConfig(String configFilePath) {
        super(configFilePath);
    }
}
