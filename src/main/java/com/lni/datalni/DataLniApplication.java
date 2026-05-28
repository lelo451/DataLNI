package com.lni.datalni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Spring Boot entry point.
 *
 * <p>{@link SpringBootServletInitializer} lets the WAR boot up under an external Tomcat
 * (or WebSphere/Liberty) — Boot auto-adds this {@code @SpringBootApplication} class as
 * the primary source, so no {@code configure()} override is needed. Running locally with
 * {@code mvn spring-boot:run} still uses the embedded Tomcat from
 * {@code spring-boot-starter-tomcat} (provided scope).
 *
 * <p>Defaults are forced in both entry points ({@code main} and {@code onStartup}) so
 * the same pt-BR locale and UTC timezone apply whether the app runs from {@code main}
 * (embedded Tomcat) or via the servlet initializer (external container).
 */
@SpringBootApplication
public class DataLniApplication extends SpringBootServletInitializer {

    private static final Locale PT_BR = new Locale("pt", "BR");

    public static void main(String[] args) {
        applyDefaults();
        SpringApplication.run(DataLniApplication.class, args);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        applyDefaults();
        super.onStartup(servletContext);
    }

    private static void applyDefaults() {
        Locale.setDefault(PT_BR);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
