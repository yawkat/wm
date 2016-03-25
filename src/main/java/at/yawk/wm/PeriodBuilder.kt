package at.yawk.wm

import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.ui.RenderElf
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.NotThreadSafe
import javax.inject.Inject

private val LOOKUP = MethodHandles.lookup()

/**
 * @author yawkat
 */
@NotThreadSafe
class PeriodBuilder @Inject constructor(private val renderElf: RenderElf, private val scheduler: Scheduler) {
    /*
     * This class takes periodic tasks and uses dark magic to reduce them to as few
     * scheduler tasks as possible while still never having a task exec where nothing is done.
     *
     * This is done to minimize RenderElf.render() calls.
     */

    private val buckets = HashSet<Bucket>()

    fun scan(o: Any) {
        for (method in o.javaClass.declaredMethods) {
            method.isAccessible = true
            scan(o, method)
        }
    }

    fun scan(o: Any, method: Method) {
        val periodic = method.getAnnotation(Periodic::class.java)
        if (periodic != null) {
            submit(LOOKUP.unreflect(method).bindTo(o),
                    Math.toIntExact(periodic.unit.toMillis(periodic.value.toLong())),
                    periodic.render)
        }
    }

    private fun submit(task: MethodHandle, interval: Int, render: Boolean) {
        val node = Node(task, render)
        for (bucket in buckets) {
            if (bucket.interval % interval == 0) {
                // smaller or equal to that bucket and divisible
                bucket.setInterval(interval)
                bucket.nodes.add(node)
                // merge other nodes into this one
                val iterator = buckets.iterator()
                while (iterator.hasNext()) {
                    val other = iterator.next()
                    if (other == bucket) {
                        continue
                    }
                    if (other.interval % bucket.interval == 0) {
                        other.setInterval(interval)
                        bucket.nodes.addAll(other.nodes)
                        iterator.remove()
                    }
                }
                return
            } else if (interval % bucket.interval == 0) {
                // greater or equal to that bucket and divisible
                bucket.nodes.add(node)
                node.tickInterval = interval / bucket.interval
                return
            }
        }
        // need a new bucket
        val bucket = Bucket()
        bucket.interval = interval
        bucket.nodes.add(node)
        buckets.add(bucket)
    }

    fun flush() {
        for (bucket in buckets) {
            bucket.initialRun()
            scheduler.scheduleAtFixedRate(bucket, bucket.interval.toLong(), bucket.interval.toLong(), TimeUnit.MILLISECONDS)
        }
    }

    private inner class Bucket : Runnable {

        val nodes = ArrayList<Node>()
        var interval: Int = 0

        internal fun setInterval(newInterval: Int) {
            if (this.interval == newInterval) {
                return
            }
            val multiplier = interval / newInterval
            assert(multiplier > 1)
            for (node in nodes) {
                node.tickInterval *= multiplier
            }
            interval = newInterval
        }

        override fun run() {
            var render = false
            for (node in nodes) {
                render = render or node.run()
            }
            if (render) {
                renderElf.render()
            }
        }

        internal fun initialRun() = nodes.forEach { it.run() }
    }

    private class Node(private val handle: MethodHandle, private val render: Boolean) {

        var tickInterval = 1
        var i = 0

        internal fun run(): Boolean {
            val run = i == 0
            if (run) {
                try {
                    PeriodBuilderInvoker.invokeVoid(handle)
                } catch (t: Throwable) {
                    log.error("Error while executing " + handle, t)
                }

            }
            i = (i + 1) % tickInterval
            return run and render
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(PeriodBuilder::class.java)
    }
}
