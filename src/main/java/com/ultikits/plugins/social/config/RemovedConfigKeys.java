package com.ultikits.plugins.social.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.ultikits.ultitools.abstracts.UltiToolsPlugin;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Reports configuration keys this module no longer reads but which are still sitting in the
 * operator's own {@code config/social.yml}.
 * <p>
 * Deleting a key from {@link SocialConfig} stops the framework writing it into a fresh file, but it
 * does nothing to the files already on disk: the framework only ever writes a declared default for a
 * key that is <em>missing</em>, so an existing install keeps the key, keeps whatever value the
 * operator gave it, and gets no indication that the value means nothing. This class is that
 * indication -- one warning per leftover key, naming the module, the file and the key, and saying
 * what became of the setting.
 *
 * @author wisdomme
 * @version 1.0.0
 */
public final class RemovedConfigKeys {

    /**
     * Every key removed from {@code config/social.yml}, mapped to the language-file key of what an
     * operator should be told about it. Insertion order is the order the warnings are emitted in.
     */
    private static final Map<String, String> REMOVED;

    static {
        Map<String, String> removed = new LinkedHashMap<String, String>();
        removed.put("notifications.friend_join_world", "removed_key_reason_friend_join_world");
        removed.put("messages.player_blocked", "removed_key_reason_blacklist_message");
        removed.put("messages.player_unblocked", "removed_key_reason_blacklist_message");
        REMOVED = Collections.unmodifiableMap(removed);
    }

    private RemovedConfigKeys() {
        // Utility class
    }

    /**
     * The keys this class knows about, in the order it reports them.
     *
     * @return an unmodifiable map of removed key path to the language-file key of the guidance printed
     *         for it
     */
    public static Map<String, String> removedKeys() {
        return REMOVED;
    }

    /**
     * Emit one warning per removed key that is still present in the operator's configuration file.
     * <p>
     * Silent when the file is absent, is not a regular file, or cannot be parsed -- there is then
     * nothing to report and nothing to be sure of. A parse failure is deliberately not reported
     * here: the framework's own config loading already fails loudly on an unparseable file, and a
     * second message from this check would only add noise to it.
     *
     * @param configFile the operator's {@code config/social.yml}; may be {@code null}
     * @param warn       where to send each warning, normally the module logger's warn method
     * @param plugin     the module, whose language file gives the warning its text
     */
    public static void warnAboutLeftovers(File configFile, Consumer<String> warn, UltiToolsPlugin plugin) {
        if (configFile == null || !configFile.isFile()) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            return;
        }
        for (Map.Entry<String, String> entry : REMOVED.entrySet()) {
            if (yaml.contains(entry.getKey())) {
                // No "[UltiSocial]" prefix: the module logger adds that itself, and the module is
                // still named in the sentence for any consumer that does not.
                String reason = "removed_key_reason_friend_join_world".equals(entry.getValue())
                        ? plugin.i18n("removed_key_reason_friend_join_world")
                        : plugin.i18n("removed_key_reason_blacklist_message");
                warn.accept(plugin.i18n("removed_key_warning")
                        .replace("{FILE}", configFile.getPath())
                        .replace("{KEY}", entry.getKey())
                        .replace("{REASON}", reason));
            }
        }
    }
}
