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
        @DisplayName("Should have friend join world notifications disabled by default")
        void notifyFriendJoinWorld() {
            SocialConfig config = createRealConfig();
            assertThat(config.isNotifyFriendJoinWorld()).isFalse();
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
        @DisplayName("Should have default GUI title")
        void guiTitle() {
            SocialConfig config = createRealConfig();
            assertThat(config.getGuiTitle()).isEqualTo("&6好友列表 &7({COUNT}/{MAX})");
        }

        @Test
        @DisplayName("Should have default friend added message")
        void friendAddedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendAddedMessage()).isEqualTo("&a你和 {PLAYER} 成为了好友！");
        }

        @Test
        @DisplayName("Should have default friend removed message")
        void friendRemovedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendRemovedMessage()).isEqualTo("&c你已删除好友 {PLAYER}");
        }

        @Test
        @DisplayName("Should have default friend online message")
        void friendOnlineMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendOnlineMessage()).isEqualTo("&a你的好友 {PLAYER} 上线了！");
        }

        @Test
        @DisplayName("Should have default friend offline message")
        void friendOfflineMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getFriendOfflineMessage()).isEqualTo("&7你的好友 {PLAYER} 下线了");
        }

        @Test
        @DisplayName("Should have default blocked message")
        void blockedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getBlockedMessage()).isEqualTo("&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系");
        }

        @Test
        @DisplayName("Should have default request sent message")
        void requestSentMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestSentMessage()).isEqualTo("&a已向 {PLAYER} 发送好友请求！");
        }

        @Test
        @DisplayName("Should have default request received message")
        void requestReceivedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestReceivedMessage()).contains("{PLAYER}");
        }

        @Test
        @DisplayName("Should have default request denied message")
        void requestDeniedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getRequestDeniedMessage()).isEqualTo("&c已拒绝 {PLAYER} 的好友请求");
        }

        @Test
        @DisplayName("Should have default max friends message")
        void maxFriendsMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getMaxFriendsMessage()).isEqualTo("&c你的好友数量已达上限！");
        }

        @Test
        @DisplayName("Should have default already friends message")
        void alreadyFriendsMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getAlreadyFriendsMessage()).contains("{PLAYER}");
        }

        @Test
        @DisplayName("Should have default player blocked message")
        void playerBlockedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getPlayerBlockedMessage()).contains("{PLAYER}");
        }

        @Test
        @DisplayName("Should have default player unblocked message")
        void playerUnblockedMessage() {
            SocialConfig config = createRealConfig();
            assertThat(config.getPlayerUnblockedMessage()).contains("{PLAYER}");
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
        @DisplayName("Should update notify friend join world")
        void setNotifyFriendJoinWorld() {
            SocialConfig config = createRealConfig();
            config.setNotifyFriendJoinWorld(true);
            assertThat(config.isNotifyFriendJoinWorld()).isTrue();
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

        @Test
        @DisplayName("Should update player blocked message")
        void setPlayerBlockedMessage() {
            SocialConfig config = createRealConfig();
            config.setPlayerBlockedMessage("&cYou blocked them!");
            assertThat(config.getPlayerBlockedMessage()).isEqualTo("&cYou blocked them!");
        }

        @Test
        @DisplayName("Should update player unblocked message")
        void setPlayerUnblockedMessage() {
            SocialConfig config = createRealConfig();
            config.setPlayerUnblockedMessage("&aUnblocked!");
            assertThat(config.getPlayerUnblockedMessage()).isEqualTo("&aUnblocked!");
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
}
