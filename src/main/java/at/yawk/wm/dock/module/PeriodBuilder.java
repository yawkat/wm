package at.yawk.wm.dock.module;

import at.yawk.wm.Scheduler;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.SneakyThrows;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
@NotThreadSafe
class PeriodBuilder {
    /*
     * This class takes periodic tasks and uses dark magic to reduce them to as few
     * scheduler tasks as possible while still never having a task exec where nothing is done.
     *
     * This is done to minimize RenderElf.render() calls.
     */

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PeriodBuilder.class);

    private final RenderElf renderElf;
    private final Set<Bucket> buckets = new HashSet<>();

    @java.beans.ConstructorProperties({ "renderElf" })
    public PeriodBuilder(RenderElf renderElf) {
        this.renderElf = renderElf;
    }

    @SneakyThrows(IllegalAccessException.class)
    public void scan(Object o, Method method) {
        Periodic periodic = method.getAnnotation(Periodic.class);
        if (periodic != null) {
            submit(LOOKUP.unreflect(method).bindTo(o),
                   Math.toIntExact(periodic.unit().toMillis(periodic.value())),
                   periodic.render());
        }
    }

    private void submit(MethodHandle task, int interval, boolean render) {
        Node node = new Node(task, render);
        for (Bucket bucket : buckets) {
            if (bucket.interval % interval == 0) {
                // smaller or equal to that bucket and divisible
                bucket.setInterval(interval);
                bucket.nodes.add(node);
                // merge other nodes into this one
                for (Iterator<Bucket> iterator = buckets.iterator(); iterator.hasNext(); ) {
                    Bucket other = iterator.next();
                    if (other == bucket) { continue; }
                    if (other.interval % bucket.interval == 0) {
                        other.setInterval(interval);
                        bucket.nodes.addAll(other.nodes);
                        iterator.remove();
                    }
                }
                return;
            } else if (interval % bucket.interval == 0) {
                // greater or equal to that bucket and divisible
                bucket.nodes.add(node);
                node.tickInterval = interval / bucket.interval;
                return;
            }
        }
        // need a new bucket
        Bucket bucket = new Bucket(renderElf);
        bucket.interval = interval;
        bucket.nodes.add(node);
        buckets.add(bucket);
    }

    public void flush(Scheduler scheduler) {
        for (Bucket bucket : buckets) {
            bucket.initialRun();
            scheduler.scheduleAtFixedRate(bucket, bucket.interval, bucket.interval, TimeUnit.MILLISECONDS);
        }
    }

    private static final class Bucket implements Runnable {
        private final RenderElf renderElf;

        private final List<Node> nodes = new ArrayList<>();
        private int interval;

        @java.beans.ConstructorProperties({ "renderElf" })
        public Bucket(RenderElf renderElf) {
            this.renderElf = renderElf;
        }

        void setInterval(int newInterval) {
            if (this.interval == newInterval) { return; }
            int multiplier = interval / newInterval;
            assert multiplier > 1;
            for (Node node : nodes) {
                node.tickInterval *= multiplier;
            }
            interval = newInterval;
        }

        @Override
        public void run() {
            boolean render = false;
            for (Node node : nodes) {
                render |= node.run();
            }
            if (render) {
                renderElf.render();
            }
        }

        void initialRun() {
            nodes.forEach(Node::run);
        }
    }

    private static final class Node {
        private final MethodHandle handle;
        private final boolean render;

        private int tickInterval = 1;
        private int i = 0;

        @java.beans.ConstructorProperties({ "handle", "render" })
        public Node(MethodHandle handle, boolean render) {
            this.handle = handle;
            this.render = render;
        }

        boolean run() {
            boolean run = i == 0;
            if (run) {
                try {
                    handle.invokeExact();
                } catch (Throwable t) {
                    log.error("Error while executing " + handle, t);
                }
            }
            i = (i + 1) % tickInterval;
            return run & render;
        }
    }
}
