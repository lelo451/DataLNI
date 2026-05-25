package com.lni.datalni;

import com.lni.datalni.ui.StageManager;
import com.lni.datalni.ui.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX application that hosts the Spring context in a single JVM.
 *
 * <ul>
 *   <li>{@link #init()} boots Spring headless (no web server) off the FX thread;</li>
 *   <li>{@link #start(Stage)} applies the theme and shows the login screen;</li>
 *   <li>{@link #stop()} closes the Spring context.</li>
 * </ul>
 */
public class DataLniFxApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder(DataLniApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        springContext.getBean(ThemeManager.class).applyDefault();
        StageManager stageManager = springContext.getBean(StageManager.class);
        stageManager.setPrimaryStage(primaryStage);
        stageManager.showLogin();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
