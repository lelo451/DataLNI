package com.lni.datalni.ui.support;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

/**
 * App-wide status feedback: a transient message shown in the status bar and a "busy" flag
 * (a counter of in-flight background tasks) the UI can bind a progress indicator to.
 * All mutations are marshalled onto the JavaFX Application Thread.
 */
@Component
public class StatusService {

    private final StringProperty message = new SimpleStringProperty("");
    private final IntegerProperty busyCount = new SimpleIntegerProperty(0);
    private final BooleanBinding busy = busyCount.greaterThan(0);
    private PauseTransition clear;

    /** Shows a message that clears itself after a few seconds. */
    public void info(String text) {
        Platform.runLater(() -> {
            message.set(text);
            if (clear != null) {
                clear.stop();
            }
            clear = new PauseTransition(Duration.seconds(4));
            clear.setOnFinished(e -> message.set(""));
            clear.playFromStart();
        });
    }

    public void busyStart() {
        Platform.runLater(() -> busyCount.set(busyCount.get() + 1));
    }

    public void busyEnd() {
        Platform.runLater(() -> busyCount.set(Math.max(0, busyCount.get() - 1)));
    }

    public ReadOnlyStringProperty messageProperty() {
        return message;
    }

    public BooleanBinding busyProperty() {
        return busy;
    }

    /** True while at least one background task is running (for binding without exposing the binding). */
    public BooleanBinding runningProperty() {
        return Bindings.createBooleanBinding(busy::get, busy);
    }
}
