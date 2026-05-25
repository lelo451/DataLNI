package com.lni.datalni.ui.support;

import com.lni.datalni.config.TaskExecutorConfig;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Runs blocking service/DB work off the JavaFX Application Thread on the shared,
 * security-context-propagating executor, then delivers the result back on the FX thread
 * via {@link Task}'s {@code succeeded}/{@code failed} handlers.
 */
@Component
public class AsyncRunner {

    private final Executor executor;

    public AsyncRunner(@Qualifier(TaskExecutorConfig.FX_EXECUTOR) Executor executor) {
        this.executor = executor;
    }

    /** Runs {@code work}, calling {@code onSuccess} on the FX thread; errors show a dialog. */
    public <T> void run(Callable<T> work, Consumer<T> onSuccess) {
        run(work, onSuccess, error -> Dialogs.error("Operation failed", error));
    }

    public <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        executor.execute(task);
    }
}
