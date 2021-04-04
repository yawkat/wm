package at.yawk.wm

import at.yawk.paste.client.Config
import at.yawk.wm.dashboard.*
import at.yawk.wm.dbus.Dbus
import at.yawk.wm.dock.module.DockBootstrap
import at.yawk.wm.dock.module.DockBuilder
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.Monitor
import at.yawk.wm.paste.PasteManager
import at.yawk.wm.tac.launcher.Launcher
import at.yawk.wm.tac.password.PasswordManager
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager
import at.yawk.wm.x.GlobalResourceRegistry
import at.yawk.wm.x.Screen
import at.yawk.wm.x.XcbConnector
import at.yawk.wm.x.font.FontCache
import at.yawk.wm.x.icon.IconManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Module
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t
import java.net.URL
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

    // connect to dbus
    val dbus = Dbus()
    dbus.connect()

    // connect to X11
    val connector = injector.getInstance(XcbConnector::class.java)
    connector.open()

    injector = injector.createChildInjector(Module { binder ->
        binder.bind(GlobalResourceRegistry::class.java).toInstance(connector.globalResourceRegistry())
        binder.bind(Screen::class.java).toInstance(connector.screen)
        binder.bind(SWIGTYPE_p_xcb_connection_t::class.java).toInstance(connector.connection)
    }, dbus)

    // connect to herbstluftwm
    val herbstClient = injector.getInstance(HerbstClient::class.java)
    herbstClient.listen()

    // initialize icon manager and font cache
    injector.getInstance(IconManager::class.java).load()
    injector.getInstance(FontCache::class.java)

    val monitors = herbstClient.listMonitors()

    // start dock
    for (monitor in monitors) {
        injector.createChildInjector(Module {
            it.bind(Monitor::class.java).toInstance(monitor)
            it.bind(RenderElf::class.java).to(DockBuilder::class.java)
        }).getInstance(DockBootstrap::class.java).startDock()
    }

    // initialize desktop for wallpaper and dashboard
    injector.getInstance(DesktopManager::class.java).init()

    // start wallpaper
    injector.getInstance(AnimatedWallpaperManager::class.java).start()

    // initialize tac uis
    injector.getInstance(Launcher::class.java).bind()
    injector.getInstance(PasteManager::class.java).bind()
    injector.getInstance(PasswordManager::class.java).bind()

    val xkcdLoader = injector.getInstance(XkcdLoader::class.java)

    // start dashboard
    for (monitor in monitors) {
        injector.createChildInjector(Module {
            it.bind(Monitor::class.java).toInstance(monitor)
            it.bind(RenderElf::class.java).to(Dashboard::class.java)
        }).getInstance(Dashboard::class.java).start()
    }

    // load xkcd
    xkcdLoader.start()
}

/**
 * @author yawkat
 */
class Main : AbstractModule() {
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
        bind(at.yawk.paste.client.Config::class.java).toInstance(Config().also {
            it.remote = URL("https://s.yawk.at")
        })

        val scheduler = scheduledExecutor()
        bind(Scheduler::class.java).toInstance(scheduler)
        bind(Executor::class.java).toInstance(scheduler)
    }
}