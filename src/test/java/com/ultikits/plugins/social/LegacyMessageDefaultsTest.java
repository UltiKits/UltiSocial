package com.ultikits.plugins.social;

import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An operator file still holding a text default an earlier version shipped (the friend-list title and
 * ten messages were Chinese) is rewritten to blank and saved, at start-up and on reload, so the
 * language file's text takes over in the server's language; any other value is the operator's and is
 * kept (maintainer ruling 2026-09-24 (d), UltiKits/UltiSocial#14).
 * <p>
 * The shipped values are copied from this module's history (every revision of {@code SocialConfig}):
 * each setting had exactly one default until this change.
 */
@DisplayName("Shipped Chinese text defaults give way to the language file")
class LegacyMessageDefaultsTest {

    private static final Map<String, String> SHIPPED = new LinkedHashMap<>();

    static {
        SHIPPED.put("guiTitle", "&6好友列表 &7({COUNT}/{MAX})");
        SHIPPED.put("friendAddedMessage", "&a你和 {PLAYER} 成为了好友！");
        SHIPPED.put("friendRemovedMessage", "&c你已删除好友 {PLAYER}");
        SHIPPED.put("friendOnlineMessage", "&a你的好友 {PLAYER} 上线了！");
        SHIPPED.put("friendOfflineMessage", "&7你的好友 {PLAYER} 下线了");
        SHIPPED.put("requestSentMessage", "&a已向 {PLAYER} 发送好友请求！");
        SHIPPED.put("requestReceivedMessage", "&e{PLAYER} 想和你成为好友！输入 /friend accept {PLAYER} 接受");
        SHIPPED.put("requestDeniedMessage", "&c已拒绝 {PLAYER} 的好友请求");
        SHIPPED.put("maxFriendsMessage", "&c你的好友数量已达上限！");
        SHIPPED.put("alreadyFriendsMessage", "&c你已经和 {PLAYER} 是好友了！");
        SHIPPED.put("blockedMessage", "&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系");
    }

    private static void set(SocialConfig config, String field, String value) throws Exception {
        Field f = SocialConfig.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(config, value);
    }

    private static String get(SocialConfig config, String field) throws Exception {
        Field f = SocialConfig.class.getDeclaredField(field);
        f.setAccessible(true);
        return (String) f.get(config);
    }

    private SocialConfig shipped() throws Exception {
        SocialConfig config = spy(new SocialConfig("config/social.yml"));
        doNothing().when(config).save();
        for (Map.Entry<String, String> e : SHIPPED.entrySet()) {
            set(config, e.getKey(), e.getValue());
        }
        return config;
    }

    private UltiSocial pluginWith(SocialConfig config) {
        UltiSocial plugin = mock(UltiSocial.class);
        when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("en"));
        when(plugin.getLogger()).thenReturn(mock(PluginLogger.class));
        when(plugin.getConfig(SocialConfig.class)).thenReturn(config);
        return plugin;
    }

    private void assertAllBlank(SocialConfig config) throws Exception {
        for (String field : SHIPPED.keySet()) {
            assertThat(get(config, field)).as(field).isEmpty();
        }
    }

    @Test
    @DisplayName("start-up blanks every shipped default and saves the file")
    void startUp() throws Exception {
        SocialConfig config = shipped();
        UltiSocial plugin = pluginWith(config);
        when(plugin.registerSelf()).thenCallRealMethod();

        plugin.registerSelf();

        assertAllBlank(config);
        verify(config).save();
    }

    @Test
    @DisplayName("/ul reload blanks every shipped default and saves the file")
    void reload() throws Exception {
        SocialConfig config = shipped();
        UltiSocial plugin = pluginWith(config);
        doCallRealMethod().when(plugin).onReload();

        plugin.onReload();

        assertAllBlank(config);
        verify(config).save();
    }

    @Test
    @DisplayName("a customised value is kept while the shipped ones beside it are blanked")
    void customisedIsKept() throws Exception {
        SocialConfig config = shipped();
        set(config, "friendAddedMessage", "&aNow friends with {PLAYER}");
        set(config, "guiTitle", SHIPPED.get("guiTitle") + " ");
        UltiSocial plugin = pluginWith(config);
        when(plugin.registerSelf()).thenCallRealMethod();

        plugin.registerSelf();

        assertThat(get(config, "friendAddedMessage")).isEqualTo("&aNow friends with {PLAYER}");
        assertThat(get(config, "guiTitle")).isEqualTo(SHIPPED.get("guiTitle") + " ");
        assertThat(Arrays.asList(get(config, "blockedMessage"), get(config, "friendOnlineMessage"))).containsOnly("");
        verify(config).save();
    }

    @Test
    @DisplayName("a file already blank is not rewritten on the next start")
    void blankIsNotRewritten() throws Exception {
        SocialConfig config = shipped();
        for (String field : SHIPPED.keySet()) {
            set(config, field, "");
        }
        UltiSocial plugin = pluginWith(config);
        when(plugin.registerSelf()).thenCallRealMethod();

        plugin.registerSelf();

        verify(config, never()).save();
    }
}
