package com.lni.datalni.ui.support;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Looks up user-facing strings from the {@code i18n/messages} bundle for the current
 * default locale (pt-BR primary, English fallback). {@link ResourceBundle} caches the
 * lookups, and resolving per call keeps it correct regardless of class-load order.
 */
public final class Messages {

    private static final String BUNDLE = "i18n/messages";

    private Messages() {
    }

    public static String get(String key) {
        return ResourceBundle.getBundle(BUNDLE, Locale.getDefault()).getString(key);
    }

    /** {@code key}'s value formatted with {@link MessageFormat} arguments ({@code {0}}, ...). */
    public static String get(String key, Object... args) {
        return MessageFormat.format(
                ResourceBundle.getBundle(BUNDLE, Locale.getDefault()).getString(key), args);
    }
}
