package com.ultikits.plugins.social.i18n;

import com.ultikits.plugins.social.config.RemovedConfigKeys;
import com.ultikits.plugins.social.service.FriendService;
import com.ultikits.ultitools.abstracts.UltiToolsPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Test support for the language sweep's two new seams.
 * <p>
 * Routing this module's text through its language file gives {@link FriendService} an {@code i18n}
 * pass-through (the GUIs, the command and the listener reach the catalogue through the service they
 * already hold) and {@link RemovedConfigKeys#warnAboutLeftovers} a plugin parameter. The tests are
 * written once and must compile and run against the code before and after that change (a revert proof
 * restores the old production files and runs these same tests), so they reach both reflectively: when
 * the new member exists it is stubbed or called, otherwise the old behaviour runs untouched.
 */
public final class SocialSeams {

    private SocialSeams() {
    }

    /** Makes a mocked {@link FriendService} answer {@code i18n} from {@code code}'s catalogue, when it has one. */
    public static void speak(FriendService mockService, String code) {
        Method i18n;
        try {
            i18n = FriendService.class.getMethod("i18n", String.class);
        } catch (NoSuchMethodException e) {
            return;
        }
        try {
            lenient().when(i18n.invoke(mockService, anyString())).thenAnswer(CatalogueText.answer(code));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot stub FriendService#i18n", e);
        }
    }

    /** {@link RemovedConfigKeys#warnAboutLeftovers}, given the plugin when the method takes one. */
    public static void warnAboutLeftovers(File configFile, Consumer<String> warn, UltiToolsPlugin plugin) {
        try {
            try {
                RemovedConfigKeys.class.getMethod("warnAboutLeftovers", File.class, Consumer.class, UltiToolsPlugin.class)
                        .invoke(null, configFile, warn, plugin);
            } catch (NoSuchMethodException e) {
                RemovedConfigKeys.class.getMethod("warnAboutLeftovers", File.class, Consumer.class)
                        .invoke(null, configFile, warn);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot call RemovedConfigKeys#warnAboutLeftovers", e);
        }
    }
}
