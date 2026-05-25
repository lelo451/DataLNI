package com.lni.datalni.ui.support;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/** Thin wrappers around JavaFX {@link Alert} for consistent, themed dialogs. */
public final class Dialogs {

    private Dialogs() {
    }

    public static void info(String header, String content) {
        show(Alert.AlertType.INFORMATION, Messages.get("dialog.info.title"), header, content);
    }

    public static void error(String header, Throwable error) {
        show(Alert.AlertType.ERROR, Messages.get("dialog.error.title"), header,
                ErrorTranslator.toMessage(error));
    }

    public static void error(String header, String content) {
        show(Alert.AlertType.ERROR, Messages.get("dialog.error.title"), header, content);
    }

    /** @return true if the user confirmed (OK). */
    public static boolean confirm(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content,
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(Messages.get("dialog.confirm.title"));
        alert.setHeaderText(header);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
