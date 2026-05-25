package com.lni.datalni.ui.support;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Inline, per-field form validation built on the Bean Validation {@link Validator}, so the
 * constraints already declared on the DTOs (and their localized messages) drive the UI
 * without duplicating any rules. Each constrained property is bound to a small error
 * {@link Label} shown beneath its field; an error clears as soon as the user edits the
 * offending field.
 *
 * @param <T> the validated DTO type
 */
public final class FormValidation<T> {

    private final Validator validator;
    private final Map<String, Label> labels = new HashMap<>();

    public FormValidation(Validator validator) {
        this.validator = validator;
    }

    /**
     * Binds a bean {@code property} to the label that displays its error, hiding the label
     * until a violation occurs. The error clears whenever any {@code clearTrigger} changes
     * (typically the field's value or text property).
     */
    public void field(String property, Label label, ObservableValue<?>... clearTriggers) {
        labels.put(property, label);
        hide(label);
        for (ObservableValue<?> trigger : clearTriggers) {
            trigger.addListener((obs, old, value) -> hide(labels.get(property)));
        }
    }

    /** Validates {@code bean}, showing the first message per property; true when valid. */
    public boolean validate(T bean) {
        clearAll();
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        Set<String> shown = new HashSet<>();
        for (ConstraintViolation<T> violation : violations) {
            String property = violation.getPropertyPath().toString();
            if (shown.add(property)) {
                show(property, violation.getMessage());
            }
        }
        return violations.isEmpty();
    }

    /** Forces a message onto a property's label (e.g. a parse error the validator can't see). */
    public void show(String property, String message) {
        Label label = labels.get(property);
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    public void clearAll() {
        labels.values().forEach(this::hide);
    }

    private void hide(Label label) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
    }
}
