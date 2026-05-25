package com.lni.datalni.ui;

import atlantafx.base.controls.ToggleSwitch;
import com.lni.datalni.security.AuthenticationService;
import com.lni.datalni.security.CurrentUser;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Main window: top bar (logout, theme toggle, module navigation), content area, status bar. */
@Component
@Scope("prototype")
public class MainController {

    private final SpringFxmlLoader fxmlLoader;
    private final CurrentUser currentUser;
    private final ThemeManager themeManager;
    private final AuthenticationService authenticationService;
    private final StageManager stageManager;

    @FXML private StackPane contentArea;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private ToggleSwitch themeToggle;
    @FXML private FontIcon themeIcon;

    public MainController(SpringFxmlLoader fxmlLoader, CurrentUser currentUser,
                          ThemeManager themeManager, AuthenticationService authenticationService,
                          StageManager stageManager) {
        this.fxmlLoader = fxmlLoader;
        this.currentUser = currentUser;
        this.themeManager = themeManager;
        this.authenticationService = authenticationService;
        this.stageManager = stageManager;
    }

    @FXML
    private void initialize() {
        userLabel.setText(currentUser.getUsername());
        roleLabel.setText(currentUser.getRolesDisplay());

        themeToggle.setSelected(themeManager.isDark());
        updateThemeIcon(themeManager.isDark());
        themeToggle.selectedProperty().addListener((obs, was, dark) -> {
            themeManager.apply(dark);
            updateThemeIcon(dark);
        });

        showGraphs();
    }

    /** Sun when dark (tap to go light), moon when light (tap to go dark). */
    private void updateThemeIcon(boolean dark) {
        themeIcon.setIconLiteral(dark ? "fas-sun" : "fas-moon");
    }

    @FXML
    private void showGraphs() {
        setContent("graph-view.fxml");
    }

    @FXML
    private void showProjects() {
        setContent("project-view.fxml");
    }

    @FXML
    private void showSustainability() {
        setContent("sustainability-view.fxml");
    }

    @FXML
    private void onLogout() {
        authenticationService.logout();
        stageManager.showLogin();
    }

    private void setContent(String fxml) {
        contentArea.getChildren().setAll(fxmlLoader.load(fxml).root());
    }
}
