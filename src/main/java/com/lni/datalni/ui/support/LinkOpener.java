package com.lni.datalni.ui.support;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Opens external links in the system browser. Tries AWT {@link Desktop} first (works on
 * Windows/macOS), then the OS launcher ({@code xdg-open}/{@code open}/{@code rundll32}) —
 * the latter is needed on Linux, where {@code Desktop.BROWSE} is usually unsupported.
 */
public final class LinkOpener {

    private LinkOpener() {
    }

    public static void open(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String target = url.matches("(?i)^[a-z][a-z0-9+.-]*://.*") ? url : "https://" + url;
        if (tryDesktop(target) || tryOsCommand(target)) {
            return;
        }
        Dialogs.error(Messages.get("link.openFailed"), target);
    }

    private static boolean tryDesktop(String target) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(target));
                return true;
            }
        } catch (Exception ignored) {
            // fall through to the OS launcher
        }
        return false;
    }

    private static boolean tryOsCommand(String target) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> command;
        if (os.contains("win")) {
            command = List.of("rundll32", "url.dll,FileProtocolHandler", target);
        } else if (os.contains("mac")) {
            command = List.of("open", target);
        } else {
            command = List.of("xdg-open", target);
        }
        try {
            new ProcessBuilder(command).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
