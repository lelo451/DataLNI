package com.lni.datalni.ui.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Persists a list of staged items off the FX thread, skipping failures, then shows an
 * "imported X / failed Y" summary and runs {@code onComplete} on the FX thread.
 */
public final class Batch {

    private Batch() {
    }

    public static <D> void saveAll(AsyncRunner async, List<D> items,
                                   Function<D, Optional<String>> persister, Runnable onComplete) {
        async.run(
                () -> {
                    int ok = 0;
                    List<String> errors = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        Optional<String> error = persister.apply(items.get(i));
                        if (error.isEmpty()) {
                            ok++;
                        } else {
                            errors.add(Messages.get("import.rowError",
                                    String.valueOf(i + 1), error.get()));
                        }
                    }
                    return new Summary(ok, errors);
                },
                summary -> {
                    String text = Messages.get("import.summary",
                            String.valueOf(summary.ok()), String.valueOf(summary.errors().size()));
                    if (!summary.errors().isEmpty()) {
                        int max = Math.min(10, summary.errors().size());
                        text += "\n\n" + String.join("\n", summary.errors().subList(0, max));
                    }
                    Dialogs.info(Messages.get("import.done"), text);
                    onComplete.run();
                });
    }

    private record Summary(int ok, List<String> errors) {
    }
}
