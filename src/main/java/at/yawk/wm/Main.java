package at.yawk.wm;

import at.yawk.yarn.Component;
import at.yawk.yarn.ComponentScan;
import at.yawk.yarn.Provides;
import at.yawk.yarn.Yarn;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        log.info("--------------------------------------------------------------------------------");
        log.info("Logging initialized, starting up...");
        try {
            start();
            log.info("Startup complete.");
        } catch (Throwable t) {
            log.error("Error during startup", t);
            return;
        }

        // wait until interrupt
        Object o = new Object();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (o) {
            o.wait();
        }
        System.exit(0);
    }

    private static void start() {
        Yarn.build(EntryPoint.class);
    }

    @ComponentScan
    interface EntryPoint {}

    ///////

    @Inject ObjectMapper objectMapper;

    @Provides
    Config config() {
        try (InputStream i = new BufferedInputStream(Files.newInputStream(Paths.get("config.yml")))) {
            return objectMapper.readValue(i, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Provides
    Scheduler scheduledExecutor() {
        AtomicInteger threadId = new AtomicInteger(0);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("Pool thread #" + threadId.incrementAndGet());
            return thread;
        };

        ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1, threadFactory);
        ExecutorService immediateService = Executors.newCachedThreadPool(threadFactory);
        return new Scheduler(scheduledService, immediateService);
    }
}
