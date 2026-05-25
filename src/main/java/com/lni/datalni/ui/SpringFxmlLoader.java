package com.lni.datalni.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Loads FXML with the controller factory wired to Spring (so controllers are managed
 * beans injected with services) and the i18n {@link ResourceBundle} attached so
 * {@code %key} references in FXML resolve.
 */
@Component
public class SpringFxmlLoader {

    private static final String FXML_DIR = "/fxml/";

    private final ApplicationContext applicationContext;
    private final ResourceBundle messages;

    public SpringFxmlLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.messages = ResourceBundle.getBundle("i18n/messages", Locale.of("pt", "BR"));
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    /** Loads {@code /fxml/<name>} and returns the root node together with its controller. */
    public <C> FxView<C> load(String name) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIR + name));
        loader.setControllerFactory(applicationContext::getBean);
        loader.setResources(messages);
        try {
            Parent root = loader.load();
            return new FxView<>(root, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load FXML: " + name, e);
        }
    }

    /** A loaded view: its root node and (Spring-managed) controller. */
    public record FxView<C>(Parent root, C controller) {
    }
}
