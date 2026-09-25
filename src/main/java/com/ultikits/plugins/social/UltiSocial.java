package com.ultikits.plugins.social;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.ultikits.plugins.social.config.ConfigTextDefaults;
import com.ultikits.plugins.social.config.RemovedConfigKeys;
import com.ultikits.plugins.social.config.SocialConfig;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;
import com.ultikits.ultitools.annotations.ConfigEntity;
import com.ultikits.ultitools.annotations.UltiToolsModule;

/**
 * UltiSocial - Friend system module.
 * Provides friend management, online status, blacklist, and social features.
 *
 * Features:
 * - Friend management with bidirectional relationships
 * - Friend request system with auto-expiration
 * - Blacklist/block functionality
 * - Friend-to-friend teleportation with cooldown
 * - Private messaging between friends
 * - Rich GUI with pagination and status indicators
 * - Favorite friends feature
 *
 * Architecture:
 * - Uses UltiTools-API v6.2.0 Query DSL for database operations
 * - Scheduled task (@Scheduled) for automatic cleanup
 * - Config validation with @Range and @NotEmpty
 * - Service-oriented design with dependency injection
 *
 * Reload is performed by the framework's final {@code reloadSelf()}, which re-reads
 * {@code config/social.yml} into {@code SocialConfig} (UltiKits/UltiSocial#13). The
 * {@link #onReload()} hook adds the removed-key warning (UltiKits/UltiSocial#15) and writes the
 * title and messages of {@code config/social.yml} in the server's language (UltiKits/UltiSocial#14).
 *
 * @author wisdomme
 * @version 1.1.0
 */
@UltiToolsModule(
    scanBasePackages = {"com.ultikits.plugins.social"}
)
public class UltiSocial extends UltiToolsPlugin {

    /**
     * {@link SocialConfig}'s file, relative to this module's folder -- its own {@link ConfigEntity}
     * value, which is the path the framework constructs that entity with. Read once from the
     * annotation so the removed-key check can never hold a second copy of the path.
     */
    private static final String CONFIG_PATH = SocialConfig.class.getAnnotation(ConfigEntity.class).value();

    @Override
    public boolean registerSelf() {
        getLogger().info(i18n("social_enabled"));
        // Deleting a key from SocialConfig does nothing to the operator's existing file, so tell
        // them about any key this version no longer reads (UltiKits/UltiSocial#15).
        warnAboutRemovedConfigKeys();
        writeConfigTextInServerLanguage();
        return true;
    }

    /**
     * Runs after the framework has re-read {@code config/social.yml}, on every reload of this
     * module -- a bare {@code /ul reload} as well as {@code /ul reload UltiSocial}. Repeats the
     * removed-key warning, so an operator who edits a key this version no longer reads and reloads
     * is told it has no effect.
     */
    @Override
    protected void onReload() {
        warnAboutRemovedConfigKeys();
        writeConfigTextInServerLanguage();
    }

    /**
     * Writes the friend-list title and every message in {@code config/social.yml} that is still built-in
     * text in the server's language and saves the file once, so the file holds what the module shows; any
     * other value is the operator's and is kept (maintainer decision 2026-09-25, UltiKits/UltiSocial#14).
     * Runs from {@link #registerSelf()} and from {@link #onReload()}, both after the module's language is
     * loaded -- never from a configuration change listener, which the framework fires before it reloads
     * the language. A value already in the current language matches nothing to replace, so a second
     * start writes nothing.
     * The text comes from this jar's own catalogue for the server's language, not from {@code i18n} (which
     * reads the operator's extracted language file first), so every value written is one the next pass
     * recognises (the text source decision of 2026-09-25).
     */
    private void writeConfigTextInServerLanguage() {
        SocialConfig config = getConfig(SocialConfig.class);
        if (config == null || !config.materializeText(
                ConfigTextDefaults.jarLanguage(SocialConfig.class, getLanguageCode())::getLocalizedText)) {
            return;
        }
        try {
            config.save();
        } catch (IOException e) {
            getLogger().warn(e, i18n("log_config_default_save_failed").replace("{FILE}", CONFIG_PATH));
        }
    }

    private void warnAboutRemovedConfigKeys() {
        // Advisory only: nothing it throws may cost the module its enable or its reload.
        try {
            RemovedConfigKeys.warnAboutLeftovers(operatorConfigFile(), getLogger()::warn, this);
        } catch (RuntimeException e) {
            getLogger().warn(e, i18n("log_removed_key_check_failed").replace("{FILE}", CONFIG_PATH));
        }
    }

    /**
     * The path of this module's configuration file, relative to its folder -- {@link #CONFIG_PATH},
     * never a copy of it. Package-private so a test can require it to equal the path
     * {@link SocialConfig} binds.
     *
     * @return {@code config/social.yml}
     */
    String operatorConfigPath() {
        return CONFIG_PATH;
    }

    /**
     * The operator's own copy of this module's configuration file.
     * <p>
     * A seam, package-private on purpose. {@code UltiToolsPlugin#getConfigFile} is {@code protected}
     * and {@code final}, so a test in this package can neither call it nor stub it, and a mocked
     * plugin returns {@code null} from it -- which means that without this method the removed-key
     * check's wiring could not be asserted at all, only its predicate.
     *
     * @return the file {@code config/social.yml} resolves to for this installation
     */
    File operatorConfigFile() {
        return getConfigFile(operatorConfigPath());
    }

    @Override
    public List<String> supported() {
        return Arrays.asList("zh", "en");
    }
}
