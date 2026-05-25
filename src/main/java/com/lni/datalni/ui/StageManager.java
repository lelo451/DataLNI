package com.lni.datalni.ui;

import com.lni.datalni.ui.imports.ImportController;
import com.lni.datalni.ui.imports.ImportField;
import com.lni.datalni.ui.imports.ParsedFile;
import com.lni.datalni.ui.imports.RowImporter;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.PreferencesStore;
import com.lni.datalni.ui.support.SvgIcons;
import com.lni.datalni.ui.support.TabularImporter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private final PreferencesStore preferences;
    private final List<Image> appIcons = loadIcons();
    private Stage primaryStage;
    private ChangeListener<Number> boundsListener;
    private ChangeListener<Boolean> maximizedListener;

    public StageManager(SpringFxmlLoader fxmlLoader, PreferencesStore preferences) {
        this.fxmlLoader = fxmlLoader;
        this.preferences = preferences;
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
        // Stop tracking before the login window resizes the shared stage, or the small
        // login geometry would clobber the saved main-window bounds.
        stopTrackingWindowState();
        SpringFxmlLoader.FxView<Object> view = fxmlLoader.load("login.fxml");
        Scene scene = new Scene(view.root());
        applyCss(scene);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
        // Defer one pulse: the window is realised and the Robot reports the real pointer.
        Platform.runLater(this::centerOnPointerMonitor);
    }

    public void showMain() {
        SpringFxmlLoader.FxView<Object> view = fxmlLoader.load("main.fxml");

        // Cap the window to the monitor under the pointer so it fits on smaller screens.
        Rectangle2D monitor = pointerScreen().getVisualBounds();
        double width = Math.min(1180, monitor.getWidth());
        double height = Math.min(760, monitor.getHeight());

        Scene scene = new Scene(view.root(), width, height);
        applyCss(scene);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        // Restore the last placement if it still lands on a connected monitor; otherwise
        // fall back to a default-sized window centred on the pointer's monitor.
        var saved = preferences.windowBounds().filter(this::isVisibleOnAnyScreen);
        if (saved.isPresent()) {
            PreferencesStore.WindowBounds b = saved.get();
            primaryStage.setX(b.x());
            primaryStage.setY(b.y());
            primaryStage.setWidth(b.width());
            primaryStage.setHeight(b.height());
            primaryStage.setMaximized(b.maximized());
            primaryStage.show();
        } else {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.show();
            Platform.runLater(this::centerOnPointerMonitor);
        }
        trackWindowState(primaryStage);
    }

    /**
     * Saves the window placement whenever the user moves, resizes, or maximises it, and
     * flushes the preferences when the window is hidden. Only non-maximized geometry is
     * stored as the restore bounds, so un-maximising returns to a sensible size.
     */
    private void trackWindowState(Stage stage) {
        boundsListener = (obs, old, value) -> {
            if (!stage.isMaximized()) {
                preferences.saveWindowBounds(stage.getX(), stage.getY(),
                        stage.getWidth(), stage.getHeight());
            }
        };
        maximizedListener = (obs, old, max) -> preferences.saveWindowMaximized(max);
        stage.xProperty().addListener(boundsListener);
        stage.yProperty().addListener(boundsListener);
        stage.widthProperty().addListener(boundsListener);
        stage.heightProperty().addListener(boundsListener);
        stage.maximizedProperty().addListener(maximizedListener);
        stage.setOnHiding(e -> preferences.flush());
    }

    /** Detaches the window-state listeners so other screens can resize the stage freely. */
    private void stopTrackingWindowState() {
        if (boundsListener != null) {
            primaryStage.xProperty().removeListener(boundsListener);
            primaryStage.yProperty().removeListener(boundsListener);
            primaryStage.widthProperty().removeListener(boundsListener);
            primaryStage.heightProperty().removeListener(boundsListener);
            boundsListener = null;
        }
        if (maximizedListener != null) {
            primaryStage.maximizedProperty().removeListener(maximizedListener);
            maximizedListener = null;
        }
        preferences.flush();
    }

    /** True when the saved rectangle overlaps any current monitor (so it's reachable). */
    private boolean isVisibleOnAnyScreen(PreferencesStore.WindowBounds b) {
        return !Screen.getScreensForRectangle(b.x(), b.y(), b.width(), b.height()).isEmpty();
    }

    /** Centres the visible primary stage on the monitor under the mouse pointer. */
    private void centerOnPointerMonitor() {
        double w = primaryStage.getWidth();
        double h = primaryStage.getHeight();
        if (Double.isNaN(w) || Double.isNaN(h) || w <= 0 || h <= 0) {
            primaryStage.centerOnScreen();
            return;
        }
        Rectangle2D b = pointerScreen().getVisualBounds();
        primaryStage.setX(b.getMinX() + Math.max(0, (b.getWidth() - w) / 2));
        primaryStage.setY(b.getMinY() + Math.max(0, (b.getHeight() - h) / 2));
    }

    /**
     * The monitor under the mouse pointer. Uses the JavaFX {@link Robot} for the global
     * pointer position (same coordinate space as {@link Screen}, unlike AWT) and never
     * throws — falls back to the primary screen.
     */
    private Screen pointerScreen() {
        try {
            Robot robot = new Robot();
            // JavaFX Robot on Linux reports the pointer relative to the primary monitor's
            // origin, so add it back to get true global coordinates (no-op when primary is
            // at 0,0).
            Rectangle2D primary = Screen.getPrimary().getBounds();
            double mx = robot.getMouseX() + primary.getMinX();
            double my = robot.getMouseY() + primary.getMinY();
            List<Screen> screens = Screen.getScreensForRectangle(mx, my, 1, 1);
            if (!screens.isEmpty()) {
                return screens.get(0);
            }
        } catch (Exception ignored) {
            // pointer unavailable -> primary screen
        }
        return Screen.getPrimary();
    }

    /**
     * Prompts for a CSV/JSON file, parses it, and opens the column-mapping import dialog
     * for the given destination fields and row importer; {@code onImported} refreshes the view.
     */
    public void openImport(String title, List<ImportField> fields,
                           RowImporter importer, Runnable onImported) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV, JSON", "*.csv", "*.json"));
        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) {
            return;
        }
        ParsedFile parsed;
        try {
            parsed = TabularImporter.parse(file);
        } catch (Exception e) {
            Dialogs.error(Messages.get("import.fileError"), e);
            return;
        }
        if (parsed.columns().isEmpty()) {
            Dialogs.error(Messages.get("import.fileError"), Messages.get("import.emptyFile"));
            return;
        }
        this.<ImportController>openModal("import-dialog.fxml", title,
                controller -> controller.configure(fields, parsed, importer, onImported));
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
