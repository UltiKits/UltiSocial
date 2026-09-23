package com.ultikits.plugins.social.config;

import com.ultikits.ultitools.annotations.ConfigEntry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * UltiKits/UltiSocial#15. Deleting {@code notifications.friend_join_world} from {@link SocialConfig}
 * stops the framework writing it into a fresh {@code social.yml} and does nothing to the files
 * already on disk: the framework writes a declared default only for a key that is missing and never
 * removes one, so every upgraded server keeps the key and whatever value its operator gave it. This
 * check is the only thing that tells that operator the value means nothing.
 * <p>
 * The positive control comes first on purpose: a check that never fires and a server with no
 * leftover key print the same empty console, so the negative cases below prove nothing on their own.
 */
@DisplayName("RemovedConfigKeys (UltiKits/UltiSocial#15)")
class RemovedConfigKeysTest {

    /** The shape the framework wrote on every server that ran an earlier version. */
    private static final String FILE_WITH_THE_REMOVED_KEY =
            "max_friends: 50\n"
            + "notifications:\n"
            + "  friend_online: true\n"
            + "  friend_offline: true\n"
            + "  friend_join_world: false\n"
            + "tp_to_friend:\n"
            + "  enabled: true\n";

    /** The same file with only the removed key taken out. */
    private static final String FILE_WITHOUT_THE_REMOVED_KEY =
            "max_friends: 50\n"
            + "notifications:\n"
            + "  friend_online: true\n"
            + "  friend_offline: true\n"
            + "tp_to_friend:\n"
            + "  enabled: true\n";

    private static File write(File dir, String body) throws IOException {
        File file = new File(dir, "social.yml");
        Files.write(file.toPath(), body.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    @DisplayName("POSITIVE CONTROL: a leftover notifications.friend_join_world produces one warning naming the module, the file and the key")
    void warnsAboutTheLeftoverKey(@TempDir File dir) throws IOException {
        File file = write(dir, FILE_WITH_THE_REMOVED_KEY);
        List<String> warnings = new ArrayList<String>();

        RemovedConfigKeys.warnAboutLeftovers(file, warnings::add);

        assertThat(warnings).hasSize(1);
        String warning = warnings.get(0);
        assertThat(warning).contains("UltiSocial");
        assertThat(warning).contains(file.getPath());
        assertThat(warning).contains("'notifications.friend_join_world'");
        assertThat(warning).contains("no longer reads");
        // What the operator can do about it: nothing replaces it, the feature is a request, and
        // the key can be deleted.
        assertThat(warning).contains("UltiKits/UltiSocial#21");
        assertThat(warning).contains("UltiKits/UltiSocial#15");
        assertThat(warning).contains("Delete the key from the file to silence this warning.");
    }

    @Test
    @DisplayName("a leftover key switched on is reported the same way as one switched off")
    void warnsWhateverTheLeftoverValue(@TempDir File dir) throws IOException {
        File file = write(dir, "notifications:\n  friend_join_world: true\n");
        List<String> warnings = new ArrayList<String>();

        RemovedConfigKeys.warnAboutLeftovers(file, warnings::add);

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0)).contains("'notifications.friend_join_world'");
    }

    @Test
    @DisplayName("no warning for the same file with only the removed key taken out")
    void silentWithoutTheKey(@TempDir File dir) throws IOException {
        File file = write(dir, FILE_WITHOUT_THE_REMOVED_KEY);
        List<String> warnings = new ArrayList<String>();

        RemovedConfigKeys.warnAboutLeftovers(file, warnings::add);

        assertThat(warnings).isEmpty();
    }

    @Test
    @DisplayName("no warning, and no exception, for a missing file, no file at all, or a file that is not YAML")
    void silentWithoutAReadableFile(@TempDir File dir) throws IOException {
        List<String> warnings = new ArrayList<String>();
        File unparseable = write(dir, "notifications:\n  friend_join_world: [unclosed\n");

        assertThatCode(() -> {
            RemovedConfigKeys.warnAboutLeftovers(new File(dir, "absent.yml"), warnings::add);
            RemovedConfigKeys.warnAboutLeftovers(null, warnings::add);
            RemovedConfigKeys.warnAboutLeftovers(dir, warnings::add);
            RemovedConfigKeys.warnAboutLeftovers(unparseable, warnings::add);
        }).doesNotThrowAnyException();

        assertThat(warnings).isEmpty();
    }

    @Test
    @DisplayName("every key the check knows about is one SocialConfig no longer declares")
    void knowsOnlyUndeclaredKeys() {
        List<String> declared = new ArrayList<String>();
        for (Field field : SocialConfig.class.getDeclaredFields()) {
            ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
            if (entry != null) {
                declared.add(entry.path());
            }
        }

        assertThat(declared).as("control: the scan sees SocialConfig's keys")
                .contains("notifications.friend_online", "notifications.friend_offline");
        assertThat(RemovedConfigKeys.removedKeys().keySet())
                .containsExactly("notifications.friend_join_world")
                .doesNotContainAnyElementsOf(declared);
    }
}
