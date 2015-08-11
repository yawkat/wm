package at.yawk.wm.dbus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Value;

/**
 * @author yawkat
 */
class DEventBus {
    private final Map<EndPoint, List<Runnable>> taskMap = new ConcurrentHashMap<>();

    void subscribe(EndPoint endPoint, Runnable listener) {
        List<Runnable> taskList = taskMap.computeIfAbsent(endPoint, e -> new CopyOnWriteArrayList<>());
        taskList.add(listener);
    }

    void postUpdate(EndPoint endPoint) {
        List<Runnable> listeners = taskMap.get(endPoint);
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    @Value
    public static final class EndPoint {
        private final Bus bus;
        private final String objectPath;
        private final String interfaceName;
        private final String property;
    }
}
