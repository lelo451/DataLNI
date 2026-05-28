package com.lni.datalni.ui.imports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * Definition of one destination field on the mapping grid: which DTO property to
 * fill, the user-facing label, whether the user must map it, and a parser that
 * coerces the raw String cell to the target type. Parsers should throw a
 * runtime exception with a message safe to show the user — the import loop
 * wraps it into a {@link RowError}.
 */
@RequiredArgsConstructor
@Getter
public class ImportFieldSpec {

    private final String fieldName;
    private final String label;
    private final boolean required;
    private final Function<String, Object> parser;
}
