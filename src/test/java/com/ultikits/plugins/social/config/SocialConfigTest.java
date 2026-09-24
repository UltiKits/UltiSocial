package com.ultikits.plugins.social.config;

import com.ultikits.plugins.social.i18n.CatalogueText;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SocialConfig Tests")
class SocialConfigTest {

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Should have max 50 friends by default")
        void maxFriends() {
            SocialConfig config = createRealConfig();
            assertThat(config.getMaxFriends()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should have 60 second request timeout by default")
        void requestTimeout() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestTimeout()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should have friend online notifications enabled by default")
        void notifyFriendOnline() {
            SocialConfig config = createRealConfig();
            assertThat(config.isNotifyFriendOnline()).isTrue();
        }

        @Test
        @DisplayName("Should have friend offline notifications enabled by default")
        void notifyFriendOffline() {
            SocialConfig config = createRealConfig();
            assertThat(config.isNotifyFriendOffline()).isTrue();
        }

        @Test
        @DisplayName("Should have teleport to friend enabled by default")
        void tpToFriendEnabled() {
            SocialConfig config = createRealConfig();
            assertThat(config.isTpToFriendEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should have 30 second teleport cooldown by default")
        void tpCooldown() {
            SocialConfig config = createRealConfig();
            assertThat(config.getTpCooldown()).isEqualTo(30);
        }

        @Test
        @DisplayName("GuiTitle is blank by default; the language file gives its text")
        void guiTitle() {
            SocialConfig config = createRealConfig();
            assertThat(config.getGuiTitle()).isEmpty();
            assertThat(CatalogueText.text("en", "gui_friend_list")).contains("{COUNT}", "{MAX}");
            assertThat(CatalogueText.text("zh", "gui_friend_list")).contains("{COUNT}", "{MAX}");
        }

        @Test
        @DisplayName("FriendAddedMessage is blank by default; the language file gives its text")
        void friendAddedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendAddedMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "friend_added")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "friend_added")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("FriendRemovedMessage is blank by default; the language file gives its text")
        void friendRemovedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendRemovedMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "friend_removed")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "friend_removed")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("FriendOnlineMessage is blank by default; the language file gives its text")
        void friendOnlineMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendOnlineMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "friend_online")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "friend_online")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("FriendOfflineMessage is blank by default; the language file gives its text")
        void friendOfflineMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendOfflineMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "friend_offline")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "friend_offline")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("BlockedMessage is blank by default; the language file gives its text")
        void blockedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getBlockedMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "blocked")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "blocked")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("RequestSentMessage is blank by default; the language file gives its text")
        void requestSentMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestSentMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "request_sent")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "request_sent")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("RequestReceivedMessage is blank by default; the language file gives its text")
        void requestReceivedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestReceivedMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "request_received")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "request_received")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("RequestDeniedMessage is blank by default; the language file gives its text")
        void requestDeniedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestDeniedMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "request_denied")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "request_denied")).contains("{PLAYER}");
        }

        @Test
        @DisplayName("MaxFriendsMessage is blank by default; the language file gives its text")
        void maxFriendsMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getMaxFriendsMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "max_friends_reached")).isNotEmpty();
            assertThat(CatalogueText.text("zh", "max_friends_reached")).isNotEmpty();
        }

        @Test
        @DisplayName("AlreadyFriendsMessage is blank by default; the language file gives its text")
        void alreadyFriendsMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getAlreadyFriendsMessage()).isEmpty();
            assertThat(CatalogueText.text("en", "already_friends")).contains("{PLAYER}");
            assertThat(CatalogueText.text("zh", "already_friends")).contains("{PLAYER}");
        }


    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Should update max friends")
        void setMaxFriends() {
            SocialConfig config = createRealConfig();
            config.setMaxFriends(100);
            assertThat(config.getMaxFriends()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should update request timeout")
        void setRequestTimeout() {
            SocialConfig config = createRealConfig();
            config.setRequestTimeout(120);
            assertThat(config.getRequestTimeout()).isEqualTo(120);
        }

        @Test
        @DisplayName("Should update notify friend online")
        void setNotifyFriendOnline() {
            SocialConfig config = createRealConfig();
            config.setNotifyFriendOnline(false);
            assertThat(config.isNotifyFriendOnline()).isFalse();
        }

        @Test
        @DisplayName("Should update notify friend offline")
        void setNotifyFriendOffline() {
            SocialConfig config = createRealConfig();
            config.setNotifyFriendOffline(false);
            assertThat(config.isNotifyFriendOffline()).isFalse();
        }

        @Test
        @DisplayName("Should update tp to friend enabled")
        void setTpToFriendEnabled() {
            SocialConfig config = createRealConfig();
            config.setTpToFriendEnabled(false);
            assertThat(config.isTpToFriendEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should update tp cooldown")
        void setTpCooldown() {
            SocialConfig config = createRealConfig();
            config.setTpCooldown(60);
            assertThat(config.getTpCooldown()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should update GUI title")
        void setGuiTitle() {
            SocialConfig config = createRealConfig();
            config.setGuiTitle("&eFriends");
            assertThat(config.getGuiTitle()).isEqualTo("&eFriends");
        }

        @Test
        @DisplayName("Should update messages")
        void setMessages() {
            SocialConfig config = createRealConfig();
            config.setFriendAddedMessage("&aNew friend!");
            assertThat(config.getFriendAddedMessage()).isEqualTo("&aNew friend!");
        }

        @Test
        @DisplayName("Should update friend removed message")
        void setFriendRemovedMessage() {
            SocialConfig config = createRealConfig();
            config.setFriendRemovedMessage("&cRemoved!");
            assertThat(config.getFriendRemovedMessage()).isEqualTo("&cRemoved!");
        }

        @Test
        @DisplayName("Should update friend online message")
        void setFriendOnlineMessage() {
            SocialConfig config = createRealConfig();
            config.setFriendOnlineMessage("&aOnline!");
            assertThat(config.getFriendOnlineMessage()).isEqualTo("&aOnline!");
        }

        @Test
        @DisplayName("Should update friend offline message")
        void setFriendOfflineMessage() {
            SocialConfig config = createRealConfig();
            config.setFriendOfflineMessage("&7Offline!");
            assertThat(config.getFriendOfflineMessage()).isEqualTo("&7Offline!");
        }

        @Test
        @DisplayName("Should update request sent message")
        void setRequestSentMessage() {
            SocialConfig config = createRealConfig();
            config.setRequestSentMessage("&aSent!");
            assertThat(config.getRequestSentMessage()).isEqualTo("&aSent!");
        }

        @Test
        @DisplayName("Should update request received message")
        void setRequestReceivedMessage() {
            SocialConfig config = createRealConfig();
            config.setRequestReceivedMessage("&eReceived!");
            assertThat(config.getRequestReceivedMessage()).isEqualTo("&eReceived!");
        }

        @Test
        @DisplayName("Should update request denied message")
        void setRequestDeniedMessage() {
            SocialConfig config = createRealConfig();
            config.setRequestDeniedMessage("&cDenied!");
            assertThat(config.getRequestDeniedMessage()).isEqualTo("&cDenied!");
        }

        @Test
        @DisplayName("Should update max friends message")
        void setMaxFriendsMessage() {
            SocialConfig config = createRealConfig();
            config.setMaxFriendsMessage("&cToo many!");
            assertThat(config.getMaxFriendsMessage()).isEqualTo("&cToo many!");
        }

        @Test
        @DisplayName("Should update already friends message")
        void setAlreadyFriendsMessage() {
            SocialConfig config = createRealConfig();
            config.setAlreadyFriendsMessage("&cAlready!");
            assertThat(config.getAlreadyFriendsMessage()).isEqualTo("&cAlready!");
        }

        @Test
        @DisplayName("Should update blocked message")
        void setBlockedMessage() {
            SocialConfig config = createRealConfig();
            config.setBlockedMessage("&cBlocked!");
            assertThat(config.getBlockedMessage()).isEqualTo("&cBlocked!");
        }


    }

    /**
     * Create a real SocialConfig using a mock path to avoid AbstractConfigEntity I/O.
     * We use Mockito spy to bypass the superclass constructor's file loading.
     */
    private SocialConfig createRealConfig() {
        // Use mock to avoid AbstractConfigEntity file I/O, then set fields
        SocialConfig config = mock(SocialConfig.class, withSettings().useConstructor("config/social.yml").defaultAnswer(CALLS_REAL_METHODS));
        return config;
    }

    @Test
    @DisplayName("the two blacklist messages nothing read are no longer settings (UltiKits/UltiSocial#23)")
    void unreadBlacklistMessagesAreRemoved() {
        for (java.lang.reflect.Field field : SocialConfig.class.getDeclaredFields()) {
            assertThat(field.getName()).isNotIn("playerBlockedMessage", "playerUnblockedMessage");
        }
    }

    @Test
    @DisplayName("no text setting rejects a blank value (maintainer ruling 2026-09-24 (d))")
    void textSettingsAcceptBlank() throws Exception {
        for (String field : new String[] {"guiTitle", "friendAddedMessage", "friendRemovedMessage",
                "friendOnlineMessage", "friendOfflineMessage", "requestSentMessage", "requestReceivedMessage",
                "requestDeniedMessage", "maxFriendsMessage", "alreadyFriendsMessage", "blockedMessage"}) {
            assertThat(SocialConfig.class.getDeclaredField(field)
                    .isAnnotationPresent(com.ultikits.ultitools.annotations.config.NotEmpty.class))
                    .as("%s must accept a blank value", field).isFalse();
        }
    }
}
