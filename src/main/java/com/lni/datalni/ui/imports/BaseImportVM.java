package com.lni.datalni.ui.imports;

import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import lombok.Getter;
import lombok.Setter;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared state + flow for the four import dialogs. Subclasses define {@link #fieldSpecs}
 * (which DTO fields exist + how to coerce a String cell to the target type) and
 * {@link #persist(Map)} (constructs the DTO and pushes it through the service).
 *
 * <p>UI lifecycle:
 * <ol>
 *   <li>User uploads a file → {@link #onUpload(Media)} parses + populates {@link #parsedFile},
 *       seeds {@link #mapping} (auto-pairs headers whose name matches a field spec, case
 *       insensitive).</li>
 *   <li>User adjusts the column dropdowns on the mapping grid.</li>
 *   <li>User clicks Importar → {@link #runImport()} iterates the parsed rows, applies the
 *       mapping + parsers, calls {@link #persist(Map)} for each, accumulates errors into
 *       {@link #failures}.</li>
 *   <li>{@link #finish(org.zkoss.zk.ui.Component)} closes the modal and posts
 *       {@link #refreshGlobalCommand()} so the list reloads.</li>
 * </ol>
 */
@Getter
@Setter
public abstract class BaseImportVM {

    /** Placeholder value used in the mapping comboboxes for "do not import this field". */
    public static final String IGNORE = "(ignorar)";

    protected ParsedFile parsedFile;
    /** {@code fieldName → sourceColumn} (or {@link #IGNORE}). Driven by the mapping UI. */
    protected Map<String, String> mapping = new LinkedHashMap<>();
    protected final ListModelList<RowError> failures = new ListModelList<>();
    protected int successCount;
    /** True after a successful upload + parse, so the mapping/import controls become active. */
    protected boolean fileLoaded;

    @Command
    @NotifyChange({"parsedFile", "fileLoaded", "mapping", "headerOptions",
            "failures", "successCount", "summary"})
    public void onUpload(@BindingParam("media") Media media) {
        if (media == null) {
            return;
        }
        try {
            TabularFileParser parser = FileParserFactory.forFile(media.getName());
            ParsedFile parsed = parser.parse(media.getName(),
                    media.isBinary() ? media.getStreamData() : toStream(media.getReaderData()));
            if (parsed.isEmpty()) {
                Messagebox.show(Messages.get("import.emptyFile"),
                        Messages.get("dialog.info.title"),
                        Messagebox.OK, Messagebox.INFORMATION);
                return;
            }
            this.parsedFile = parsed;
            this.fileLoaded = true;
            this.failures.clear();
            this.successCount = 0;
            seedMapping(parsed.getHeaders());
        } catch (IllegalArgumentException ex) {
            Messagebox.show(ex.getMessage(),
                    Messages.get("dialog.error.title"),
                    Messagebox.OK, Messagebox.ERROR);
        } catch (IOException ex) {
            Messagebox.show(Messages.get("import.fileError") + ": " + ex.getMessage(),
                    Messages.get("dialog.error.title"),
                    Messagebox.OK, Messagebox.ERROR);
        }
    }

    private java.io.InputStream toStream(java.io.Reader reader) throws IOException {
        // Media#getReaderData branch (text content w/o known binary type) — buffer
        // into a byte stream so the parsers can read in a uniform way.
        StringBuilder buf = new StringBuilder();
        char[] tmp = new char[4096];
        int n;
        while ((n = reader.read(tmp)) >= 0) {
            buf.append(tmp, 0, n);
        }
        return new java.io.ByteArrayInputStream(buf.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Auto-pair source columns with fields whose name matches case-insensitively. */
    private void seedMapping(List<String> headers) {
        mapping = new LinkedHashMap<>();
        for (ImportFieldSpec spec : getFieldSpecs()) {
            String hit = headers.stream()
                    .filter(h -> h.equalsIgnoreCase(spec.getFieldName())
                            || h.equalsIgnoreCase(spec.getLabel()))
                    .findFirst().orElse(IGNORE);
            mapping.put(spec.getFieldName(), hit);
        }
    }

    @Command
    @NotifyChange({"failures", "successCount", "summary"})
    public void runImport() {
        if (parsedFile == null || parsedFile.isEmpty()) {
            return;
        }
        // Verify the user mapped every required field.
        List<String> missingRequired = getFieldSpecs().stream()
                .filter(ImportFieldSpec::isRequired)
                .filter(s -> IGNORE.equals(mapping.getOrDefault(s.getFieldName(), IGNORE)))
                .map(ImportFieldSpec::getLabel)
                .collect(java.util.stream.Collectors.toList());
        if (!missingRequired.isEmpty()) {
            Messagebox.show(Messages.get("import.requiredMissing",
                            String.join(", ", missingRequired)),
                    Messages.get("dialog.info.title"),
                    Messagebox.OK, Messagebox.INFORMATION);
            return;
        }

        failures.clear();
        successCount = 0;
        List<Map<String, String>> rows = parsedFile.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            int rowNumber = i + 2; // +1 for 1-based, +1 for header row.
            try {
                Map<String, Object> values = coerce(row, rowNumber);
                persist(values);
                successCount++;
            } catch (Exception ex) {
                failures.add(new RowError(rowNumber, ErrorTranslator.translate(ex)));
            }
        }
        if (failures.isEmpty()) {
            // No errors → close the dialog and refresh the underlying list.
            BindUtils.postGlobalCommand(null, null, refreshGlobalCommand(), null);
            org.zkoss.zk.ui.Executions.getCurrent()
                    .getDesktop()
                    .getFirstPage()
                    .getFellowIfAny("importWindow");
        } else {
            // Partial: keep the modal open on the Failures tab so the user can review.
            BindUtils.postGlobalCommand(null, null, refreshGlobalCommand(), null);
            Messagebox.show(Messages.get("import.summary", successCount, failures.size()),
                    Messages.get("dialog.info.title"),
                    Messagebox.OK, Messagebox.INFORMATION);
        }
    }

    /**
     * Resolve each mapped source column for one row and run the field's parser.
     * The parser is expected to throw {@link RuntimeException} on invalid input.
     */
    private Map<String, Object> coerce(Map<String, String> row, int rowNumber) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (ImportFieldSpec spec : getFieldSpecs()) {
            String source = mapping.getOrDefault(spec.getFieldName(), IGNORE);
            if (IGNORE.equals(source)) {
                continue;
            }
            String raw = row.get(source);
            if (raw == null || raw.isBlank()) {
                if (spec.isRequired()) {
                    throw new IllegalArgumentException(
                            Messages.get("import.rowError", rowNumber,
                                    spec.getLabel() + ": " + Messages.get("error.required")));
                }
                continue;
            }
            try {
                values.put(spec.getFieldName(), spec.getParser().apply(raw.trim()));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(spec.getLabel() + ": " + ex.getMessage(), ex);
            }
        }
        return values;
    }

    @Command
    public void close(@org.zkoss.bind.annotation.ContextParam(
            org.zkoss.bind.annotation.ContextType.VIEW) org.zkoss.zk.ui.Component view) {
        view.detach();
    }

    // -- Computed binding helpers -------------------------------------------

    /** Combobox model for each mapping row: source columns + the "ignore" sentinel. */
    public List<String> getHeaderOptions() {
        List<String> opts = new java.util.ArrayList<>();
        opts.add(IGNORE);
        if (parsedFile != null) {
            opts.addAll(parsedFile.getHeaders());
        }
        return opts;
    }

    public String getSummary() {
        if (parsedFile == null) {
            return "";
        }
        if (successCount == 0 && failures.isEmpty()) {
            return parsedFile.getRows().size() + " linha(s) prontas para importar.";
        }
        return Messages.get("import.summary", successCount, failures.size());
    }

    // -- Subclass hooks -----------------------------------------------------

    public abstract List<ImportFieldSpec> getFieldSpecs();

    /** Build the DTO from the coerced values and call the matching service create. */
    protected abstract void persist(Map<String, Object> values);

    /** Global command posted on success so the parent list view reloads. */
    protected abstract String refreshGlobalCommand();
}
