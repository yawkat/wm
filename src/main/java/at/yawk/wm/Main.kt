package at.yawk.wm

import at.yawk.paste.client.Config
import at.yawk.wm.dashboard.Dashboard
import at.yawk.wm.dashboard.DashboardBootstrap
import at.yawk.wm.dashboard.DesktopManager
import at.yawk.wm.dashboard.XkcdLoader
import at.yawk.wm.dbus.Dbus
import at.yawk.wm.dbus.MediaPlayer
import at.yawk.wm.dbus.NetworkManager
import at.yawk.wm.dbus.Power
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockBootstrap
import at.yawk.wm.dock.module.DockBuilder
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.Monitor
import at.yawk.wm.paste.PasteManager
import at.yawk.wm.tac.launcher.Launcher
import at.yawk.wm.tac.password.PasswordManager
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager
import at.yawk.wm.x.XcbConnector
import at.yawk.wm.x.font.FontCache
import at.yawk.wm.x.icon.IconManager
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

private val log = org.slf4j.LoggerFactory.getLogger("at.yawk.wm.Main")

fun main() {
    log.info("--------------------------------------------------------------------------------")
    log.info("Logging initialized, starting up...")
    try {
        Main.runtime()
        log.info("Startup complete.")
    } catch (t: Throwable) {
        log.error("Error during startup", t)
        return
    }

    // wait until interrupt
    val o = Object()
    synchronized (o) {
        o.wait()
    }
    System.exit(0)
}

private object Main {
    val mainModule = MainModule()
    val mainComponent = DaggerMainComponent.builder().mainModule(mainModule).build()
    val herbstClient = mainComponent.herbstClient()
    val iconManager = mainComponent.iconManager()
    val passwordManager = mainComponent.passwordManager()

    init {
        // initialize tac uis
        mainComponent.launcher().bind()
        mainComponent.pasteManager().bind()
        passwordManager.bind()

        iconManager.load()
    }

    fun runtime() {
        Util.emulateAtBuildTime = false

        // connect to X11
        mainComponent.connector().open()

        passwordManager.configure()

        // connect to herbstluftwm
        herbstClient.listen()
        herbstClient.runDelayedCommands()

        iconManager.render()

        val monitors = herbstClient.listMonitors()
        // start dock
        for (monitor in monitors) {
            val monitorContext = mainComponent.dockBuilder().create(monitor)
            monitorContext.dockBootstrap().startDock()
        }

        // initialize desktop for wallpaper and dashboard
        mainComponent.desktopManager().init()

        // start wallpaper
        mainComponent.animatedWallpaperManager().load()
        mainComponent.animatedWallpaperManager().show()

        val xkcdLoader = mainComponent.xkcdLoader()

        // start dashboard
        for (monitor in monitors) {
            val monitorContext = mainComponent.dashboardBuilder().create(monitor)
            monitorContext.dashboard().start()
        }

        // connect to dbus
        val dbus = Dbus()
        dbus.connect()
        mainModule.setDbus(dbus)

        // load xkcd
        xkcdLoader.start()
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

@Singleton
@Component(modules = [MainModule::class])
private interface MainComponent {
    fun connector(): XcbConnector
    fun herbstClient(): HerbstClient
    fun iconManager(): IconManager
    fun desktopManager(): DesktopManager
    fun animatedWallpaperManager(): AnimatedWallpaperManager
    fun launcher(): Launcher
    fun pasteManager(): PasteManager
    fun passwordManager(): PasswordManager
    fun xkcdLoader(): XkcdLoader

    fun dockBuilder(): DockComponent.Factory
    fun dashboardBuilder(): DashboardComponent.Factory

    @Component.Builder
    interface Builder {
        fun mainModule(mainModule: MainModule): Builder
        fun build(): MainComponent
    }
}

@Subcomponent(modules = [DockModule::class])
@PerMonitor
private interface DockComponent {
    fun dockBootstrap(): DockBootstrap

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance monitor: Monitor): DockComponent
    }
}

@Subcomponent(modules = [DashboardModule::class])
@PerMonitor
private interface DashboardComponent {
    fun dashboard(): DashboardBootstrap

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance monitor: Monitor): DashboardComponent
    }
}

@Module(subcomponents = [DockComponent::class, DashboardComponent::class])
private class MainModule {
    private val scheduler = scheduledExecutor()
    @get:Provides
    val pasteConfig = Config().also {
        it.remote = URL("https://s.yawk.at")
    }
    private val mediaPlayer = MediaPlayer.LateInit()
    private val networkManager = NetworkManager.LateInit()
    private val power = Power.LateInit()

    fun setDbus(dbus: Dbus) {
        Util.requireRuntime()
        mediaPlayer.setDelegate(dbus.mediaPlayer())
        networkManager.setDelegate(dbus.networkManager())
        power.setDelegate(dbus.power())
    }

    @Provides
    fun mediaPlayer(): MediaPlayer = mediaPlayer

    @Provides
    fun networkManager(): NetworkManager = networkManager

    @Provides
    fun power(): Power = power

    @Provides
    fun scheduler(): Scheduler {
        return scheduler
    }

    @Provides
    fun executor(): Executor {
        Util.requireRuntime()
        return scheduler
    }

    @Provides
    fun fontSource(fontCache: FontCache): FontSource = fontCache
}

@Module
private interface DockModule {
    @Binds
    fun renderElf(dock: DockBuilder): RenderElf
}

@Module
private interface DashboardModule {
    @Binds
    fun renderElf(dashboard: Dashboard): RenderElf
}