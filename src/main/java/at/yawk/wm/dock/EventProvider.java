package at.yawk.wm.dock;

import java.util.function.Consumer;

/**
 * @author yawkat
 */
public interface EventProvider {
    <E> void addListener(Class<E> eventType, Consumer<E> handler);
}
