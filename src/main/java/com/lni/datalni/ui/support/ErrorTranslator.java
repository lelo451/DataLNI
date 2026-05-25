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
            return "You do not have permission to perform this action.";
        }
        if (cause instanceof AuthenticationException) {
            return "Authentication failed. Check your username and password.";
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
            return "A database error occurred. Please try again or contact support.";
        }
        String message = cause.getMessage();
        return (message == null || message.isBlank())
                ? "An unexpected error occurred." : message;
    }

    private static String formatViolation(ConstraintViolation<?> violation) {
        return "• " + violation.getPropertyPath() + ": " + violation.getMessage();
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
