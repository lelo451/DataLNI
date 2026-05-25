package com.lni.datalni.ui.support;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Persists user-interface preferences across runs using the platform user preference
 * store ({@link Preferences} — registry on Windows, {@code ~/.java/.userPrefs} on Linux,
 * defaults plist on macOS), so no extra files are written next to the app.
 */
@Component
public class PreferencesStore {

    private static final String THEME_DARK = "theme.dark";
    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_W = "window.w";
    private static final String WINDOW_H = "window.h";
    private static final String WINDOW_MAX = "window.max";

    private final Preferences prefs = Preferences.userRoot().node("com/lni/datalni");

    public boolean isDark() {
        return prefs.getBoolean(THEME_DARK, false);
    }

    public void setDark(boolean dark) {
        prefs.putBoolean(THEME_DARK, dark);
    }

    /** The last saved window placement, if any (absent on first run or after a reset). */
    public Optional<WindowBounds> windowBounds() {
        double x = prefs.getDouble(WINDOW_X, Double.NaN);
        double y = prefs.getDouble(WINDOW_Y, Double.NaN);
        double w = prefs.getDouble(WINDOW_W, Double.NaN);
        double h = prefs.getDouble(WINDOW_H, Double.NaN);
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(w) || Double.isNaN(h)
                || w <= 0 || h <= 0) {
            return Optional.empty();
        }
        return Optional.of(new WindowBounds(x, y, w, h, prefs.getBoolean(WINDOW_MAX, false)));
    }

    /** Records the restore (non-maximized) position and size of the window. */
    public void saveWindowBounds(double x, double y, double width, double height) {
        prefs.putDouble(WINDOW_X, x);
        prefs.putDouble(WINDOW_Y, y);
        prefs.putDouble(WINDOW_W, width);
        prefs.putDouble(WINDOW_H, height);
    }

    public void saveWindowMaximized(boolean maximized) {
        prefs.putBoolean(WINDOW_MAX, maximized);
    }

    /** Forces pending changes to disk; call before the JVM exits to be safe. */
    public void flush() {
        try {
            prefs.flush();
        } catch (Exception ignored) {
            // best effort: a failed flush just means preferences aren't carried over
        }
    }

    /** Window placement: restore bounds plus whether the window was maximized. */
    public record WindowBounds(double x, double y, double width, double height,
                               boolean maximized) {
    }
}
