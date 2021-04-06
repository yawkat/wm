package at.yawk.wm.dbus

import at.yawk.dbus.client.annotation.Destination
import at.yawk.dbus.client.annotation.SystemBus
import at.yawk.dbus.client.annotation.ObjectPath
import java.util.concurrent.TimeUnit
import at.yawk.dbus.client.annotation.GetProperty
import at.yawk.dbus.client.annotation.Interface
import at.yawk.dbus.client.annotation.Listener
import at.yawk.dbus.client.annotation.Member
import at.yawk.dbus.client.annotation.Timeout
import java.lang.Runnable

@SystemBus
@Destination("org.freedesktop.NetworkManager")
@ObjectPath("/org/freedesktop/NetworkManager")
@Interface("org.freedesktop.NetworkManager")
@Timeout(value = 1, unit = TimeUnit.SECONDS)
interface NetworkManager {
    @get:Member("Connectivity")
    @get:GetProperty
    val connectivity: Int

    @Member("StateChanged")
    @Listener
    fun onStateChanged(listener: Runnable)

    class LateInit : NetworkManager {
        @Volatile
        private var _delegate: NetworkManager? = null
        private var onStateChanged = listOf<Runnable>()

        @Synchronized
        fun setDelegate(delegate: NetworkManager) {
            require(_delegate == null)
            _delegate = delegate
            onStateChanged.forEach { delegate.onStateChanged(it) }
            onStateChanged = emptyList()
        }

        override val connectivity: Int
            get() = _delegate?.connectivity ?: 0

        @Synchronized
        override fun onStateChanged(listener: Runnable) {
            onStateChanged = onStateChanged + listener
        }
    }
}