package at.yawk.wm;

import at.yawk.wm.dbus.Dbus;
import at.yawk.wm.dock.module.DockBootstrap;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.paste.PasteManager;
import at.yawk.wm.style.StyleConfig;
import at.yawk.wm.tac.TacConfig;
import at.yawk.wm.tac.launcher.Launcher;
import at.yawk.wm.tac.launcher.LauncherConfig;
import at.yawk.wm.tac.password.PasswordConfig;
import at.yawk.wm.tac.password.PasswordManager;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager;
import at.yawk.wm.x.GlobalResourceRegistry;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.icon.IconConfig;
import at.yawk.wm.x.icon.IconManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;

/**
 * @author yawkat
 */
@Slf4j
public class Main extends AbstractModule {
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
        Injector injector = Guice.createInjector(new Main());

        Dbus dbus = new Dbus();
        dbus.connect();

        //noinspection resource
        XcbConnector connector = injector.getInstance(XcbConnector.class);
        connector.open();

        injector = injector.createChildInjector(binder -> {
            binder.bind(GlobalResourceRegistry.class).toInstance(connector.globalResourceRegistry());
            binder.bind(Screen.class).toInstance(connector.getScreen());
            binder.bind(SWIGTYPE_p_xcb_connection_t.class).toInstance(connector.getConnection());
        }, dbus);

        HerbstClient herbstClient = injector.getInstance(HerbstClient.class);
        herbstClient.listen();

        injector.getInstance(IconManager.class).load();
        injector.getInstance(DockBootstrap.class).startDock();
        injector.getInstance(AnimatedWallpaperManager.class).start();

        injector.getInstance(Launcher.class).bind();
        injector.getInstance(PasteManager.class).setupKeys();
        injector.getInstance(PasswordManager.class).bind();
    }

    ///////

    private final ObjectMapper objectMapper = JacksonProvider.createYamlObjectMapper();

    private Config config() {
        try (InputStream i = new BufferedInputStream(Files.newInputStream(Paths.get("config.yml")))) {
            return objectMapper.readValue(i, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Scheduler scheduledExecutor() {
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

    @Override
    protected void configure() {
        Config config = config();

        bind(Config.class).toInstance(config);
        bind(StyleConfig.class).toInstance(config.getStyle());
        bind(DockConfig.class).toInstance(config.getDock());
        bind(TacConfig.class).toInstance(config.getTac());
        bind(LauncherConfig.class).toInstance(config.getLauncher());
        bind(PasswordConfig.class).toInstance(config.getPassword());
        bind(AnimatedWallpaperConfig.class).toInstance(config.getWallpaper());
        bind(at.yawk.paste.client.Config.class).toInstance(config.getPaste());
        bind(IconConfig.class).toInstance(config.getIcon());

        bind(ObjectMapper.class).toInstance(objectMapper);

        Scheduler scheduler = scheduledExecutor();
        bind(Scheduler.class).toInstance(scheduler);
        bind(Executor.class).toInstance(scheduler);
    }
}
