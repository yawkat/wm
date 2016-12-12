package at.yawk.wm

import java.util.concurrent.TimeUnit

/**
 * @author yawkat
 */
class TimedCache<T>(
        maxAge: Long,
        maxAgeUnit: TimeUnit,
        private val updater: (T?) -> T
) {
    private val maxAgeMillis = maxAgeUnit.toMillis(maxAge)

    private var value: T? = null
    private var lastRefreshTime = 0

    fun get(): T {
        synchronized(this) {
            val now = System.currentTimeMillis()
            if (lastRefreshTime < now - maxAgeMillis || value == null) {
                value = updater(value)
            }
            return value!!
        }
    }
}