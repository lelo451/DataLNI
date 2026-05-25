package com.lni.datalni.ui.imports;

import java.util.Map;
import java.util.Optional;

/**
 * Imports a single mapped row (destination-field key -> value). Implementations build the
 * DTO and call the service. Runs on a worker thread.
 *
 * @return empty on success, or a user-facing error message if the row was skipped.
 */
@FunctionalInterface
public interface RowImporter {

    Optional<String> importRow(Map<String, String> values);
}
