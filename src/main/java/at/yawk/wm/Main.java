package at.yawk.wm;

import at.yawk.wm.dock.module.DockBuilder;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class Main {
    public static void main(String[] args) throws InterruptedException {
        start();

        // wait until interrupt
        Object o = new Object();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (o) {
            o.wait();
        }
    }

    private static void start() {
        Yarn.build(EntryPoint.class).dockBuilder().start();
    }

    @ComponentScan
    interface EntryPoint {
        DockBuilder dockBuilder();
    }

    ///////

    @Inject ObjectMapper objectMapper;

    @Provides
    Config config() {
        try (InputStream i = new BufferedInputStream(Files.newInputStream(Paths.get("config.json")))) {
            return objectMapper.readValue(i, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Provides
    ScheduledExecutorService scheduledExecutor() {
        return new ScheduledThreadPoolExecutor(1) {
            AtomicInteger threadId = new AtomicInteger(0);

            {
                setThreadFactory(r -> {
                    Thread thread = new Thread(r);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.setName("Pool thread #" + threadId.incrementAndGet());
                    return thread;
                });
                setMaximumPoolSize(16);
                setKeepAliveTime(20, TimeUnit.SECONDS);
            }
        };
    }
}
