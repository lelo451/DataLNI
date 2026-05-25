package com.lni.datalni.ui;

import com.lni.datalni.ui.support.SvgIcons;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Owns the primary {@link Stage} and switches the application between the login screen
 * and the main window. Controllers navigate by calling {@link #showMain()} /
 * {@link #showLogin()}.
 */
@Component
public class StageManager {

    private static final String APP_CSS = "/css/app.css";
    private static final String APP_ICON = "/images/app-icon.svg";

    private final SpringFxmlLoader fxmlLoader;
    private final List<Image> appIcons = loadIcons();
    private Stage primaryStage;

    public StageManager(SpringFxmlLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("DataLNI");
        applyIcon(primaryStage);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showLogin() {
        SpringFxmlLoader.FxView<Object> view = fxmlLoader.load("login.fxml");
        Scene scene = new Scene(view.root());
        applyCss(scene);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void showMain() {
        SpringFxmlLoader.FxView<Object> view = fxmlLoader.load("main.fxml");
        Scene scene = new Scene(view.root(), 1180, 760);
        applyCss(scene);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Loads a form FXML into an application-modal dialog, lets the caller configure the
     * (Spring-managed) controller, hands the controller its stage if it is
     * {@link DialogAware}, then blocks until the dialog is closed.
     */
    public <C> void openModal(String fxml, String title, Consumer<C> configurer) {
        SpringFxmlLoader.FxView<C> view = fxmlLoader.load(fxml);
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        applyIcon(dialog);
        Scene scene = new Scene(view.root());
        applyCss(scene);
        dialog.setScene(scene);
        if (view.controller() instanceof DialogAware aware) {
            aware.setDialogStage(dialog);
        }
        configurer.accept(view.controller());

        // Size the window to its content rather than a fixed size, and keep it fitted as
        // the content's preferred size changes (e.g. when a validation message appears).
        dialog.setResizable(false);
        dialog.sizeToScene();
        view.root().layoutBoundsProperty().addListener((obs, old, bounds) -> dialog.sizeToScene());

        dialog.showAndWait();
    }

    private void applyIcon(Stage stage) {
        stage.getIcons().addAll(appIcons);
    }

    /** Render the SVG app icon at a few sizes so the OS picks the sharpest for each use. */
    private List<Image> loadIcons() {
        List<Image> icons = new ArrayList<>();
        for (int size : new int[]{256, 128, 64, 32}) {
            Image icon = SvgIcons.render(APP_ICON, size);
            if (icon != null) {
                icons.add(icon);
            }
        }
        return icons;
    }

    private void applyCss(Scene scene) {
        var css = getClass().getResource(APP_CSS);
        if (css != null) {
            scene.getStylesheets().add(Objects.requireNonNull(css).toExternalForm());
        }
    }
}
