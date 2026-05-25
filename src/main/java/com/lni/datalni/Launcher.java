package com.lni.datalni;

import javafx.application.Application;

/**
 * Process entry point. Deliberately <b>not</b> a {@link javafx.application.Application}
 * subclass: launching JavaFX from a non-Application {@code main} avoids the
 * "JavaFX runtime components are missing" error when the app is packaged as a fat jar.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(DataLniFxApp.class, args);
    }
}
