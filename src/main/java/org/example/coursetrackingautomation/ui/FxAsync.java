package org.example.coursetrackingautomation.ui;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Runs potentially blocking work off the JavaFX UI thread using {@link Task}.
 *
 * <p>All UI callbacks are marshalled back to the UI thread via {@link Platform#runLater(Runnable)}
 * to keep controllers/coordinators explicit about thread boundaries.</p>
 */
public final class FxAsync {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new DaemonThreadFactory());

    private FxAsync() {
    }

    /**
     * Runs work in a background thread.
     *
     * @param work background work to execute
     * @param onSuccess success callback (UI thread)
     * @param onFailure failure callback (UI thread)
     * @param <T> result type
     */
    public static <T> void runAsync(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Objects.requireNonNull(work, "work");
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onFailure, "onFailure");

        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            Platform.runLater(() -> onFailure.accept(failure == null ? new RuntimeException("Bilinmeyen hata") : failure));
        });

        EXECUTOR.execute(task);
    }

    /**
     * Runs work in a background thread.
     *
     * @param work background work to execute
     * @param onSuccess success callback (UI thread)
     * @param onFailure failure callback (UI thread)
     */
    public static void runAsync(Runnable work, Runnable onSuccess, Consumer<Throwable> onFailure) {
        Objects.requireNonNull(work, "work");
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onFailure, "onFailure");

        runAsync(
            () -> {
                work.run();
                return Boolean.TRUE;
            },
            ignored -> onSuccess.run(),
            onFailure
        );
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger COUNTER = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("fx-async-" + COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
