package com.ultikits.plugins.social;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
 * {@link #onReload()} hook adds only the removed-key warning (UltiKits/UltiSocial#15).
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
        getLogger().info("UltiSocial v1.1.0 has been enabled!");
        // Deleting a key from SocialConfig does nothing to the operator's existing file, so tell
        // them about any key this version no longer reads (UltiKits/UltiSocial#15).
        warnAboutRemovedConfigKeys();
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
    }

    private void warnAboutRemovedConfigKeys() {
        // Advisory only: nothing it throws may cost the module its enable or its reload.
        try {
            RemovedConfigKeys.warnAboutLeftovers(operatorConfigFile(), getLogger()::warn);
        } catch (RuntimeException e) {
            getLogger().warn(e, "Could not check " + CONFIG_PATH
                    + " for removed configuration keys; the module continues without that check.");
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
