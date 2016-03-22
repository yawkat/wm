package at.yawk.wm

import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

private fun locked(runnable: Runnable): Runnable {
    val lock = ReentrantLock(true)
    return Runnable {
        lock.lock()
        try {
            runnable.run()
        } finally {
            lock.unlock()
        }
    }
}

/**
 * Wrapper for [ScheduledExecutorService] that logs task exceptions and never fails.

 * @author yawkat
 */
class Scheduler(
        // we use a scheduled service for timing and then delegate execution to a cached thread pool
        private val scheduledService: ScheduledExecutorService,
        private val immediateService: Executor
) : Executor {

    fun scheduleAtFixedRate(task: Runnable, initialDelay: Long, interval: Long, unit: TimeUnit): Future<*> {
        return scheduledService.scheduleAtFixedRate(delegating(locked(task)), initialDelay, interval, unit)
    }

    fun schedule(task: Runnable, delay: Long, unit: TimeUnit): Future<*> {
        return scheduledService.schedule(delegating(task), delay, unit)
    }

    override fun execute(task: Runnable) {
        delegating(task).run()
    }

    private fun delegating(task: Runnable): Runnable {
        return Runnable {
            immediateService.execute {
                try {
                    task.run()
                } catch (t: Throwable) {
                    log.error("Error in task", t)
                }
            }
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(Scheduler::class.java)
    }
}
