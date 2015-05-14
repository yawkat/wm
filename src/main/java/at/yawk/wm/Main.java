package at.yawk.wm;

import at.yawk.wm.dock.module.DockBuilder;
import at.yawk.wm.dock.module.DockModule;
import at.yawk.wm.json.JacksonProvider;
import at.yawk.wm.x.XcbConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
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
import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Module(
        includes = { XcbConnector.class, JacksonProvider.class },
        library = true
)
public class Main {
    public static void main(String[] args) throws InterruptedException {
        JacksonProvider jacksonProvider = new JacksonProvider();
        ObjectGraph graph = ObjectGraph.create(
                new XcbConnector(),
                jacksonProvider,
                new Main(jacksonProvider.objectMapper()),
                new DockModule()
        );

        graph.get(DockBuilder.class).start();

        // wait until interrupt
        Object o = new Object();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (o) {
            o.wait();
        }
    }

    ///////

    private Main(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private final ObjectMapper objectMapper;

    @Provides
    @Singleton
    Config config() {
        try (InputStream i = new BufferedInputStream(Files.newInputStream(Paths.get("config.json")))) {
            return objectMapper.readValue(i, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Provides
    @Singleton
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
