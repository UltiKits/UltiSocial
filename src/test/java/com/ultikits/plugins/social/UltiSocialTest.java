package com.ultikits.plugins.social;

import com.ultikits.ultitools.interfaces.impl.logger.PluginLogger;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UltiSocial Main Class Tests")
class UltiSocialTest {

    @Test
    @DisplayName("registerSelf should return true")
    void registerSelf() throws Exception {
        UltiSocial plugin = mock(UltiSocial.class);
        PluginLogger logger = mock(PluginLogger.class);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.registerSelf()).thenCallRealMethod();

        boolean result = plugin.registerSelf();

        assertThat(result).isTrue();
        verify(logger).info("UltiSocial v1.1.0 has been enabled!");
    }

    /**
     * UltiTools 6.3.0 makes {@code unregisterSelf()} and {@code reloadSelf()} final template
     * methods that always run the framework's own steps (config reload and language refresh on
     * reload; command and listener unregistration on unload) around the module's hooks. This
     * module's former overrides only logged a literal line and never called {@code super}, so
     * {@code /ul reload UltiSocial} reported success without re-reading anything
     * (UltiKits/UltiSocial#13). They are deleted outright; this test pins that neither is
     * declared again.
     */
    @Test
    @DisplayName("declares neither framework lifecycle template method (UltiKits/UltiSocial#13)")
    void declaresNeitherLifecycleTemplateMethod() {
        List<String> declared = new ArrayList<>();
        for (Method method : UltiSocial.class.getDeclaredMethods()) {
            declared.add(method.getName());
        }

        assertThat(declared).doesNotContain("unregisterSelf", "reloadSelf");
    }

    @Test
    @DisplayName("supported should return zh and en")
    void supported() throws Exception {
        UltiSocial plugin = mock(UltiSocial.class);
        when(plugin.supported()).thenCallRealMethod();

        List<String> langs = plugin.supported();

        assertThat(langs).containsExactly("zh", "en");
    }

    @Nested
    @DisplayName("Annotation Tests")
    class AnnotationTests {

        @Test
        @DisplayName("should have @UltiToolsModule annotation")
        void shouldHaveModuleAnnotation() {
            assertThat(UltiSocial.class.isAnnotationPresent(
                com.ultikits.ultitools.annotations.UltiToolsModule.class
            )).isTrue();
        }

        @Test
        @DisplayName("should scan correct packages")
        void shouldScanCorrectPackages() {
            com.ultikits.ultitools.annotations.UltiToolsModule annotation =
                UltiSocial.class.getAnnotation(
                    com.ultikits.ultitools.annotations.UltiToolsModule.class
                );

            assertThat(annotation.scanBasePackages()).contains("com.ultikits.plugins.social");
        }
    }
}
