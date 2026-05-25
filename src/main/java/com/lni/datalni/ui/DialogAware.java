package com.lni.datalni.ui;

import javafx.stage.Stage;

/** Implemented by modal form controllers so the opener can hand them their own stage. */
public interface DialogAware {

    void setDialogStage(Stage stage);
}
