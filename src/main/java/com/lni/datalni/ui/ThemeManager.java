package com.lni.datalni.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import org.springframework.stereotype.Component;

/**
 * Applies the AtlantaFX user-agent stylesheet. Primer Light is the default; the menu
 * offers a Primer Dark toggle. Switching themes is a single static stylesheet swap.
 */
@Component
public class ThemeManager {

    private boolean dark = false;

    public void applyDefault() {
        dark = false;
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    public boolean isDark() {
        return dark;
    }

    public void toggle() {
        dark = !dark;
        Application.setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }
}
