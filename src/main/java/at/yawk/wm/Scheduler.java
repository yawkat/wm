package at.yawk.wm;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for {@link ScheduledExecutorService} that logs task exceptions and never fails.
 *
 * @author yawkat
 */
@Slf4j
@RequiredArgsConstructor
public class Scheduler implements Executor {
    // we use a scheduled service for timing and then delegate execution to a cached thread pool
    private final ScheduledExecutorService scheduledService;
    private final Executor immediateService;

    public Future<?> scheduleAtFixedRate(Runnable task, long initialDelay, long interval, TimeUnit unit) {
        return scheduledService.scheduleAtFixedRate(delegating(task), initialDelay, interval, unit);
    }

    public Future<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledService.schedule(delegating(task), delay, unit);
    }

    @Override
    public void execute(Runnable task) {
        delegating(task).run();
    }

    private Runnable delegating(Runnable task) {
        return () -> immediateService.execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                log.error("Error in task", t);
            }
        });
    }

    private static Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                log.error("Error in task", t);
            }
        };
    }
}
