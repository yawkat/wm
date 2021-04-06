package at.yawk.wm.dbus

import at.yawk.dbus.client.annotation.Destination
import at.yawk.dbus.client.annotation.SystemBus
import at.yawk.dbus.client.annotation.ObjectPath
import java.util.concurrent.TimeUnit
import java.lang.Runnable
import at.yawk.dbus.client.annotation.GetProperty
import at.yawk.dbus.client.annotation.Interface
import at.yawk.dbus.client.annotation.Listener
import at.yawk.dbus.client.annotation.Member
import at.yawk.dbus.client.annotation.Timeout

/**
 * @author yawkat
 */
@SystemBus
@Destination("org.freedesktop.UPower")
@ObjectPath("/org/freedesktop/UPower/devices/DisplayDevice")
@Interface("org.freedesktop.UPower.Device")
@Timeout(value = 1, unit = TimeUnit.SECONDS)
interface Power {
    @Interface("org.freedesktop.DBus.Properties")
    @Member("PropertiesChanged")
    @Listener
    fun onPropertiesChanged(listener: Runnable)

    @get:GetProperty
    @get:Member("IsPresent")
    val isPresent: Boolean

    /**
     * http://upower.freedesktop.org/docs/Device.html#Device:State
     */
    @get:GetProperty
    @get:Member("State")
    val state: Int

    /**
     * @return seconds or 0 if charging
     */
    @get:GetProperty
    @get:Member("TimeToEmpty")
    val timeToEmpty: Long

    /**
     * @return seconds or 0 if not charging
     */
    @get:GetProperty
    @get:Member("TimeToFull")
    val timeToFull: Long

    @get:GetProperty
    @get:Member("Percentage")
    val percentage: Double

    class LateInit : Power {
        @Volatile
        private var _delegate: Power? = null
        private var onPropertiesChanged = listOf<Runnable>()

        @Synchronized
        fun setDelegate(delegate: Power) {
            require(_delegate == null)
            _delegate = delegate
            onPropertiesChanged.forEach { delegate.onPropertiesChanged(it) }
            onPropertiesChanged = emptyList()
        }

        override val isPresent: Boolean
            get() = _delegate?.isPresent ?: false
        override val state: Int
            get() = _delegate?.state ?: 0
        override val timeToEmpty: Long
            get() = 0
        override val timeToFull: Long
            get() = 0
        override val percentage: Double
            get() = 0.0

        @Synchronized
        override fun onPropertiesChanged(listener: Runnable) {
            onPropertiesChanged = onPropertiesChanged + listener
        }
    }
}