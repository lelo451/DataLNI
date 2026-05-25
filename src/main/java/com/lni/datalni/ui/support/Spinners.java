package com.lni.datalni.ui.support;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/** Configures editable integer spinners so they never hold a {@code null} value. */
public final class Spinners {

    private Spinners() {
    }

    /**
     * Installs an integer value factory and makes the spinner robust when editable:
     * typed text is committed on focus loss, and a {@code null} value (e.g. an empty
     * editor) is reverted — otherwise {@code increment()/decrement()} unbox {@code null}
     * and throw a {@link NullPointerException}.
     */
    public static void integer(Spinner<Integer> spinner, int min, int max, int initial) {
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        factory.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                factory.setValue(oldValue != null ? oldValue : min);
            }
        });

        spinner.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) {
                commitEditorText(spinner, min);
            }
        });
    }

    private static void commitEditorText(Spinner<Integer> spinner, int fallback) {
        String text = spinner.getEditor().getText();
        try {
            spinner.getValueFactory().setValue(
                    spinner.getValueFactory().getConverter().fromString(text));
        } catch (RuntimeException ex) {
            spinner.getValueFactory().setValue(fallback);   // restore a valid value
        }
    }
}
