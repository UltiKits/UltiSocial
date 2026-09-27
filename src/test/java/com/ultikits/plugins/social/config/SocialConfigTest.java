package com.ultikits.plugins.social.config;

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
}
