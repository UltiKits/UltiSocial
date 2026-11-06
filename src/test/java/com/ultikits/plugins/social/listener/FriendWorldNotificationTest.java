package com.ultikits.plugins.social.listener;

import com.ultikits.plugins.social.UltiSocialTestHelper;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.i18n.SocialSeams;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.services.NotificationService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Regression for #21: discover the Bukkit handler, not a test-only notification method. */
class FriendWorldNotificationTest {
    private SocialListener listener;
    private FriendService service;
    private SocialConfig config;
    private NotificationService notifications;
    private Player arrival;
    private Player friend;
    private World destination;

    @BeforeEach
    void setUp() throws Exception {
        UltiSocialTestHelper.setUp();
        listener = new SocialListener();
        service = mock(FriendService.class);
        config = new SocialConfig("config/social.yml");
        notifications = mock(NotificationService.class);
        UltiSocialTestHelper.setField(listener, "friendService", service);
        UltiSocialTestHelper.setField(listener, "notificationService", notifications);
        when(service.getConfig()).thenReturn(config);
        SocialSeams.speak(service, "en");
        destination = mock(World.class);
        when(destination.getName()).thenReturn("survival");
        arrival = player("Arriving", destination);
        friend = player("Friend", destination);
        when(friend.canSee(arrival)).thenReturn(true);
        when(service.areFriends(any(), any())).thenReturn(true);
        doReturn(Arrays.asList(arrival, friend))
                .when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();
    }

    @AfterEach
    void tearDown() throws Exception {
        UltiSocialTestHelper.tearDown();
    }

    private Player player(String name, World world) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(world);
        return player;
    }

    private Method worldHandler() {
        Method[] handlers = Arrays.stream(SocialListener.class.getMethods())
                .filter(m -> m.isAnnotationPresent(EventHandler.class))
                .filter(m -> Arrays.equals(m.getParameterTypes(), new Class<?>[]{PlayerChangedWorldEvent.class}))
                .toArray(Method[]::new);
        assertThat(handlers).as("one real world-change Bukkit handler for #21").hasSize(1);
        return handlers[0];
    }

    @Test
    void registersWorldChangeHandler() {
        worldHandler();
    }

    private final java.util.concurrent.atomic.AtomicLong now = new java.util.concurrent.atomic.AtomicLong(-100);

    private void enable() throws Exception {
        config.setNotifyFriendWorldChange(true);
        UltiSocialTestHelper.setField(listener, "nanoTime", (java.util.function.LongSupplier) now::get);
    }

    private void changeWorld() throws Exception {
        worldHandler().invoke(listener, new PlayerChangedWorldEvent(arrival, mock(World.class)));
    }

    @Test
    void defaultDisabledAndThirtySecondCooldown() throws Exception {
        assertThat(config.isNotifyFriendWorldChange()).isFalse();
        assertThat(config.getFriendWorldChangeCooldown()).isEqualTo(30);
        changeWorld();
        verifyNoInteractions(notifications);
    }

    @Test
    void notifiesOnlyFriendsInDestinationNeverSelfOrOtherWorld() throws Exception {
        enable();
        Player stranger = player("Stranger", destination);
        Player distant = player("Distant", mock(World.class));
        Player secondFriend = player("Second", destination);
        when(stranger.canSee(arrival)).thenReturn(true);
        when(secondFriend.canSee(arrival)).thenReturn(true);
        when(service.areFriends(stranger.getUniqueId(), arrival.getUniqueId())).thenReturn(false);
        doReturn(Arrays.asList(arrival, friend, stranger, distant, secondFriend))
                .when(UltiSocialTestHelper.getMockServer()).getOnlinePlayers();
        changeWorld();
        verify(notifications).sendMessageNotification(friend, "§aYour friend Arriving entered your world (survival)!");
        verify(notifications).sendMessageNotification(secondFriend, "§aYour friend Arriving entered your world (survival)!");
        verifyNoMoreInteractions(notifications);
    }

    @Test
    void respectsBidirectionalBlockCheckAndVisibility() throws Exception {
        enable();
        when(service.isBlocked(friend.getUniqueId(), arrival.getUniqueId())).thenReturn(true);
        changeWorld();
        verifyNoInteractions(notifications);
        when(service.isBlocked(friend.getUniqueId(), arrival.getUniqueId())).thenReturn(false);
        when(friend.canSee(arrival)).thenReturn(false);
        changeWorld();
        verifyNoInteractions(notifications);
        when(friend.canSee(arrival)).thenReturn(true);
        changeWorld();
        verify(notifications).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void respectsExplicitRecipientDenialAndGrant() throws Exception {
        enable();
        when(friend.isPermissionSet("ultisocial.notify.world")).thenReturn(true);
        when(friend.hasPermission("ultisocial.notify.world")).thenReturn(false);
        changeWorld();
        verifyNoInteractions(notifications);
        when(friend.hasPermission("ultisocial.notify.world")).thenReturn(true);
        changeWorld();
        verify(notifications).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void respectsExplicitArrivingPlayerDenialAndGrant() throws Exception {
        enable();
        when(arrival.isPermissionSet("ultisocial.notify.world")).thenReturn(true);
        changeWorld();
        verifyNoInteractions(notifications);
        when(arrival.hasPermission("ultisocial.notify.world")).thenReturn(true);
        changeWorld();
        verify(notifications).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void usesMonotonicCooldownAcrossWorldsWithoutExtendingOnSuppressedEvents() throws Exception {
        enable();
        changeWorld();
        World other = mock(World.class);
        when(other.getName()).thenReturn("lobby");
        when(arrival.getWorld()).thenReturn(other);
        when(friend.getWorld()).thenReturn(other);
        now.addAndGet(java.util.concurrent.TimeUnit.SECONDS.toNanos(30) - 1);
        changeWorld();
        verify(notifications, times(1)).sendMessageNotification(eq(friend), anyString());
        now.incrementAndGet();
        changeWorld();
        verify(notifications, times(2)).sendMessageNotification(eq(friend), anyString());
        verify(notifications).sendMessageNotification(friend, "§aYour friend Arriving entered your world (lobby)!");
    }

    @Test
    void emptyAudienceDoesNotConsumeCooldown() throws Exception {
        enable();
        when(service.areFriends(any(), any())).thenReturn(false);
        changeWorld();
        verifyNoInteractions(notifications);
        when(service.areFriends(any(), any())).thenReturn(true);
        changeWorld();
        verify(notifications).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void zeroCooldownAndLiveToggleApplyImmediately() throws Exception {
        enable();
        changeWorld();
        config.setNotifyFriendWorldChange(false);
        now.addAndGet(java.util.concurrent.TimeUnit.SECONDS.toNanos(30));
        changeWorld();
        verify(notifications, times(1)).sendMessageNotification(eq(friend), anyString());
        config.setNotifyFriendWorldChange(true);
        config.setFriendWorldChangeCooldown(0);
        changeWorld();
        changeWorld();
        verify(notifications, times(3)).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void quitClearsThrottleEvenWhenOfflineNoticesDisabled() throws Exception {
        enable();
        config.setNotifyFriendOffline(false);
        changeWorld();
        listener.onPlayerQuit(new org.bukkit.event.player.PlayerQuitEvent(arrival, "quit"));
        changeWorld();
        verify(notifications, times(2)).sendMessageNotification(eq(friend), anyString());
        verify(service).clearCache(arrival.getUniqueId());
    }

    @Test
    void eachArrivingPlayerHasIndependentCooldown() throws Exception {
        enable();
        changeWorld();
        Player second = player("Second", destination);
        when(friend.canSee(second)).thenReturn(true);
        when(arrival.canSee(second)).thenReturn(false);
        worldHandler().invoke(listener, new PlayerChangedWorldEvent(second, mock(World.class)));
        verify(notifications, times(2)).sendMessageNotification(eq(friend), anyString());
    }

    @Test
    void loginDoesNotSendWorldArrivalNotification() throws Exception {
        enable();
        config.setNotifyFriendOnline(false);
        listener.onPlayerJoin(new org.bukkit.event.player.PlayerJoinEvent(arrival, "join"));
        verifyNoInteractions(notifications);
    }

    @Test
    void fallsBackToChatInChineseWithSinglePassPlaceholders() throws Exception {
        enable();
        UltiSocialTestHelper.setField(listener, "notificationService", null);
        SocialSeams.speak(service, "zh");
        when(destination.getName()).thenReturn("&a{PLAYER}");
        changeWorld();
        verify(friend).sendMessage("§a你的好友 Arriving 进入了你所在的世界（&a{PLAYER}）！");
        verifyNoInteractions(notifications);
    }

    @Test
    void freshConfigDeclaresOptInAndCooldown() {
        assertThat(Arrays.stream(SocialConfig.class.getDeclaredFields())
                .map(f -> f.getAnnotation(com.ultikits.ultitools.annotations.ConfigEntry.class))
                .filter(java.util.Objects::nonNull).map(e -> e.path()))
                .contains("notifications.friend_world_change.enabled", "notifications.friend_world_change.cooldown");
    }
}
