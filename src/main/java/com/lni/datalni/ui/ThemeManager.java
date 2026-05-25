package com.lni.datalni.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.lni.datalni.ui.support.PreferencesStore;
import javafx.application.Application;
import org.springframework.stereotype.Component;

/**
 * Applies the AtlantaFX user-agent stylesheet. Primer Light is the default; the menu
 * offers a Primer Dark toggle. The chosen theme is remembered across runs via
 * {@link PreferencesStore}. Switching themes is a single static stylesheet swap.
 */
@Component
public class ThemeManager {

    private final PreferencesStore preferences;
    private boolean dark;

    public ThemeManager(PreferencesStore preferences) {
        this.preferences = preferences;
        this.dark = preferences.isDark();
    }

    /** Applies the theme remembered from the previous run (light on first run). */
    public void applyDefault() {
        apply(dark);
    }

    public boolean isDark() {
        return dark;
    }

    public void toggle() {
        apply(!dark);
    }

    /** Sets the theme to dark or light and remembers the choice. */
    public void apply(boolean dark) {
        this.dark = dark;
        preferences.setDark(dark);
        Application.setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }
}
