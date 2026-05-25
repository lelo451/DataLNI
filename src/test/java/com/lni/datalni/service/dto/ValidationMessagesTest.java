package com.lni.datalni.service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that constraint messages resolve from the ValidationMessages bundles:
 * pt-BR (default) from {@code ValidationMessages_pt_BR.properties} and the English
 * fallback from the base {@code ValidationMessages.properties}.
 */
class ValidationMessagesTest {

    private Set<String> messages(Locale locale) {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(locale);
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                Validator validator = factory.getValidator();
                return validator.validate(GraphDto.builder().build()).stream()  // title is blank
                        .map(jakarta.validation.ConstraintViolation::getMessage)
                        .collect(Collectors.toSet());
            }
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void titleRequiredMessageIsPortugueseUnderPtBr() {
        assertThat(messages(Locale.of("pt", "BR"))).contains("O título é obrigatório");
    }

    @Test
    void titleRequiredMessageFallsBackToEnglish() {
        assertThat(messages(Locale.ENGLISH)).contains("Title is required");
    }
}
