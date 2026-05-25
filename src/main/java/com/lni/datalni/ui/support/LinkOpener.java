package com.lni.datalni.ui.support;

import java.awt.Desktop;
import java.net.URI;

/** Opens external links in the system browser, tolerating unsupported environments. */
public final class LinkOpener {

    private LinkOpener() {
    }

    public static void open(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String target = url.matches("(?i)^[a-z][a-z0-9+.-]*://.*") ? url : "https://" + url;
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(target));
            } else {
                Dialogs.info("Open link", target);
            }
        } catch (Exception e) {
            Dialogs.error("Could not open link", e);
        }
    }
}
