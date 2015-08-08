package at.yawk.wm;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for {@link ScheduledExecutorService} that logs task exceptions and never fails.
 *
 * @author yawkat
 */
@Slf4j
public class Scheduler implements Executor {
    private final ScheduledExecutorService service;

    public Scheduler(ScheduledExecutorService service) {
        this.service = service;
    }

    public Future<?> scheduleAtFixedRate(Runnable task, long initialDelay, long interval, TimeUnit unit) {
        return service.scheduleAtFixedRate(wrap(task), initialDelay, interval, unit);
    }

    public Future<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return service.schedule(wrap(task), delay, unit);
    }

    public void execute(Runnable task) {
        service.execute(wrap(task));
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
