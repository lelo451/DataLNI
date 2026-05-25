package com.lni.datalni.ui.support;

import com.lni.datalni.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.stream.Collectors;

/**
 * Turns backend exceptions into user-friendly messages so the UI never leaks stack
 * traces (SPEC §12). Unwraps the common Spring/JavaFX {@code Task} wrappers first.
 */
public final class ErrorTranslator {

    private ErrorTranslator() {
    }

    public static String toMessage(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof AccessDeniedException) {
            return Messages.get("error.permission");
        }
        if (cause instanceof AuthenticationException) {
            return Messages.get("error.authentication");
        }
        if (cause instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                    .map(ErrorTranslator::formatViolation)
                    .collect(Collectors.joining("\n"));
        }
        if (cause instanceof NotFoundException) {
            return cause.getMessage();
        }
        if (cause instanceof DataAccessException) {
            return Messages.get("error.database");
        }
        String message = cause.getMessage();
        return (message == null || message.isBlank())
                ? Messages.get("error.unexpected") : message;
    }

    private static String formatViolation(ConstraintViolation<?> violation) {
        // The messages are self-describing (they name the field), so just bullet them.
        return "• " + violation.getMessage();
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        // Unwrap layers added by reflective service invocation / JavaFX Task.
        while ((current instanceof java.lang.reflect.UndeclaredThrowableException
                || current.getClass().getSimpleName().equals("CompletionException"))
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
