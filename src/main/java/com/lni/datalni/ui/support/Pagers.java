package com.lni.datalni.ui.support;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Refreshes a first/prev/next/last button group and a "Page X of Y" label for a page of
 * results, disabling the navigation buttons at the ends of the range.
 */
public final class Pagers {

    private Pagers() {
    }

    public static void update(Button first, Button prev, Button next, Button last, Label info,
                              int page, int totalPages) {
        info.setText(Messages.get("page.info", page + 1, Math.max(totalPages, 1)));
        boolean atFirst = page <= 0;
        boolean atLast = page >= totalPages - 1;
        first.setDisable(atFirst);
        prev.setDisable(atFirst);
        next.setDisable(atLast);
        last.setDisable(atLast);
    }
}
