package com.lni.datalni.ui.support;

import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/** Maps backend exceptions to user-facing messages from the i18n bundle. */
public final class ErrorTranslator {

    private ErrorTranslator() {
    }

    public static String translate(Throwable error) {
        // Unwrap one layer: MVVM commands wrap thrown exceptions.
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                return ((ConstraintViolationException) cause).getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n"));
            }
            if (cause instanceof AccessDeniedException) {
                return Messages.get("error.permission");
            }
            if (cause instanceof DataAccessException) {
                return Messages.get("error.database");
            }
            cause = cause.getCause();
        }
        return error.getMessage() != null ? error.getMessage() : Messages.get("error.unexpected");
    }
}
