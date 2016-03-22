package at.yawk.wm

import at.yawk.wm.dbus.Dbus
import at.yawk.wm.dock.module.DockBootstrap
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.paste.PasteManager
import at.yawk.wm.style.StyleConfig
import at.yawk.wm.tac.TacConfig
import at.yawk.wm.tac.launcher.Launcher
import at.yawk.wm.tac.launcher.LauncherConfig
import at.yawk.wm.tac.password.PasswordConfig
import at.yawk.wm.tac.password.PasswordManager
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager
import at.yawk.wm.x.GlobalResourceRegistry
import at.yawk.wm.x.Screen
import at.yawk.wm.x.XcbConnector
import at.yawk.wm.x.icon.IconConfig
import at.yawk.wm.x.icon.IconManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Module
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t
import java.io.BufferedInputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

private val log = org.slf4j.LoggerFactory.getLogger(Main::class.java)

fun main(args: Array<String>) {
    log.info("--------------------------------------------------------------------------------")
    log.info("Logging initialized, starting up...")
    try {
        start()
        log.info("Startup complete.")
    } catch (t: Throwable) {
        log.error("Error during startup", t)
        return
    }

    // wait until interrupt
    val o = Object()
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (o) {
        o.wait()
    }
    System.exit(0)
}

private fun start() {
    var injector = Guice.createInjector(Main())

    val dbus = Dbus()
    dbus.connect()

    //noinspection resource
    val connector = injector.getInstance(XcbConnector::class.java)
    connector.open()

    injector = injector.createChildInjector(Module { binder ->
        binder.bind(GlobalResourceRegistry::class.java).toInstance(connector.globalResourceRegistry())
        binder.bind(Screen::class.java).toInstance(connector.screen)
        binder.bind(SWIGTYPE_p_xcb_connection_t::class.java).toInstance(connector.connection)
    }, dbus)

    val herbstClient = injector.getInstance(HerbstClient::class.java)
    herbstClient.listen()

    injector.getInstance(IconManager::class.java).load()
    injector.getInstance(DockBootstrap::class.java).startDock()
    injector.getInstance(AnimatedWallpaperManager::class.java).start()

    injector.getInstance(Launcher::class.java).bind()
    injector.getInstance(PasteManager::class.java).setupKeys()
    injector.getInstance(PasswordManager::class.java).bind()
}

/**
 * @author yawkat
 */
class Main : AbstractModule() {
    private val objectMapper = JacksonProvider.createYamlObjectMapper()

    private fun config(): Config {
        try {
            BufferedInputStream(Files.newInputStream(Paths.get("config.yml"))).use { i -> return objectMapper.readValue(i, Config::class.java) }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    private fun scheduledExecutor(): Scheduler {
        val threadId = AtomicInteger(0)
        val threadFactory = ThreadFactory { r ->
            val thread = Thread(r)
            thread.priority = Thread.MIN_PRIORITY
            thread.name = "Pool thread #" + threadId.incrementAndGet()
            thread
        }

        val scheduledService = Executors.newScheduledThreadPool(1, threadFactory)
        val immediateService = Executors.newCachedThreadPool(threadFactory)
        return Scheduler(scheduledService, immediateService)
    }

    override fun configure() {
        val config = config()

        bind(Config::class.java).toInstance(config)
        bind(StyleConfig::class.java).toInstance(config.style)
        bind(DockConfig::class.java).toInstance(config.dock)
        bind(TacConfig::class.java).toInstance(config.tac)
        bind(LauncherConfig::class.java).toInstance(config.launcher)
        bind(PasswordConfig::class.java).toInstance(config.password)
        bind(AnimatedWallpaperConfig::class.java).toInstance(config.wallpaper)
        bind(at.yawk.paste.client.Config::class.java).toInstance(config.paste)
        bind(IconConfig::class.java).toInstance(config.icon)

        bind(ObjectMapper::class.java).toInstance(objectMapper)

        val scheduler = scheduledExecutor()
        bind(Scheduler::class.java).toInstance(scheduler)
        bind(Executor::class.java).toInstance(scheduler)
    }
}
