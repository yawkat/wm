package at.yawk.wm.dbus

import at.yawk.dbus.client.annotation.Call
import at.yawk.dbus.client.annotation.Destination
import at.yawk.dbus.client.annotation.SessionBus
import at.yawk.dbus.client.annotation.ObjectPath
import java.util.concurrent.TimeUnit
import at.yawk.dbus.client.annotation.GetProperty
import at.yawk.dbus.client.annotation.Interface
import at.yawk.dbus.client.annotation.Listener
import at.yawk.dbus.client.annotation.Member
import at.yawk.dbus.client.annotation.Timeout
import at.yawk.dbus.protocol.`object`.DbusObject

@SessionBus
@Destination("org.mpris.MediaPlayer2.spotify")
@ObjectPath("/org/mpris/MediaPlayer2")
@Interface("org.mpris.MediaPlayer2.Player")
@Timeout(value = 1, unit = TimeUnit.SECONDS)
interface MediaPlayer {
    @Call
    @Member("PlayPause")
    fun playPause()

    @Call
    @Member("Stop")
    fun stop()

    @Call
    @Member("Previous")
    fun previous()

    @Call
    @Member("Next")
    operator fun next()

    @get:Member("Metadata")
    @get:GetProperty
    val metadata: Map<String, DbusObject>

    @Interface("org.freedesktop.DBus.Properties")
    @Member("PropertiesChanged")
    @Listener
    fun onPropertiesChanged(listener: Runnable)

    class LateInit : MediaPlayer {
        @Volatile
        private var _delegate: MediaPlayer? = null
        private var onPropertiesChanged = listOf<Runnable>()

        @Synchronized
        fun setDelegate(delegate: MediaPlayer) {
            require(_delegate == null)
            _delegate = delegate
            onPropertiesChanged.forEach { delegate.onPropertiesChanged(it) }
            onPropertiesChanged = emptyList()
        }

        override fun playPause() {
            _delegate?.playPause()
        }

        override fun stop() {
            _delegate?.stop()}

        override fun previous() {
            _delegate?.previous()}

        override fun next() {
            _delegate?.next()}

        override val metadata: Map<String, DbusObject>
            get() = _delegate?.metadata ?: emptyMap()

        @Synchronized
        override fun onPropertiesChanged(listener: Runnable) {
            onPropertiesChanged = onPropertiesChanged + listener
        }
    }
}