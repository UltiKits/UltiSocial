package com.ultikits.plugins.social;

import com.ultikits.plugins.social.i18n.CatalogueText;
import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The console lines {@link UltiSocial} writes at start-up follow the framework's {@code language}
 * setting (UltiKits/UltiSocial#14), driven through {@code registerSelf} with the module's {@code i18n}
 * answering from the shipped Chinese catalogue. Before the language sweep they were fixed English text.
 * In this package because {@link UltiSocial#operatorConfigFile()} is package-private.
 */
@DisplayName("UltiSocial console lines follow the language setting")
class ConsoleLanguageTest {

    private static String zh(String key) {
        String text = CatalogueText.entries("zh").get(key);
        return text == null ? "<lang/zh has no " + key + ">" : text;
    }

    private UltiSocial plugin(PluginLogger logger) {
        UltiSocial plugin = mock(UltiSocial.class);
        when(plugin.i18n(anyString())).thenAnswer(CatalogueText.answer("zh"));
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.registerSelf()).thenCallRealMethod();
        return plugin;
    }

    @Test
    @DisplayName("the enable line and a leftover removed key are reported in Chinese")
    void enableAndRemovedKey(@TempDir File dir) throws IOException {
        File file = new File(dir, "social.yml");
        Files.write(file.toPath(), "notifications:\n  friend_join_world: true\n".getBytes(StandardCharsets.UTF_8));
        PluginLogger logger = mock(PluginLogger.class);
        UltiSocial plugin = plugin(logger);
        when(plugin.operatorConfigFile()).thenReturn(file);

        plugin.registerSelf();

        ArgumentCaptor<String> info = ArgumentCaptor.forClass(String.class);
        verify(logger, atLeast(0)).info(info.capture());
        assertThat(info.getAllValues()).containsExactly(zh("social_enabled"));
        ArgumentCaptor<String> warn = ArgumentCaptor.forClass(String.class);
        verify(logger, atLeast(0)).warn(warn.capture());
        assertThat(warn.getAllValues()).containsExactly(zh("removed_key_warning")
                .replace("{FILE}", file.getPath())
                .replace("{KEY}", "notifications.friend_join_world")
                .replace("{REASON}", zh("removed_key_reason_friend_join_world")));
    }

    @Test
    @DisplayName("a removed-key check that cannot run is reported in Chinese")
    void checkFailed() {
        PluginLogger logger = mock(PluginLogger.class);
        UltiSocial plugin = plugin(logger);
        when(plugin.operatorConfigFile()).thenThrow(new UncheckedIOException(new IOException("disk unavailable")));

        plugin.registerSelf();

        ArgumentCaptor<String> warn = ArgumentCaptor.forClass(String.class);
        verify(logger).warn(any(Throwable.class), warn.capture());
        assertThat(warn.getValue()).isEqualTo(zh("log_removed_key_check_failed").replace("{FILE}", "config/social.yml"));
    }
}
