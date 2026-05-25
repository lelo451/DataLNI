package com.lni.datalni.ui;

import com.lni.datalni.security.AuthenticationService;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Startup login screen. Authenticates via {@link AuthenticationService}, then navigates. */
@Component
@Scope("prototype")
public class LoginController {

    private final AuthenticationService authenticationService;
    private final StageManager stageManager;
    private final AsyncRunner async;

    @FXML private ImageView logo;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    public LoginController(AuthenticationService authenticationService, StageManager stageManager,
                           AsyncRunner async) {
        this.authenticationService = authenticationService;
        this.stageManager = stageManager;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        logo.setImage(SvgIcons.render("/images/app-icon.svg", 256));
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        char[] password = passwordField.getText() == null
                ? new char[0] : passwordField.getText().toCharArray();
        if (username.isEmpty() || password.length == 0) {
            showError("Enter username and password.");
            return;
        }
        setBusy(true);
        async.run(
                () -> {
                    authenticationService.login(username, password);
                    return null;
                },
                ok -> {
                    setBusy(false);
                    stageManager.showMain();
                },
                error -> {
                    setBusy(false);
                    showError(ErrorTranslator.toMessage(error));
                });
    }

    private void setBusy(boolean busy) {
        Platform.runLater(() -> {
            loginButton.setDisable(busy);
            usernameField.setDisable(busy);
            passwordField.setDisable(busy);
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
