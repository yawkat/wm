package at.yawk.wm.x;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author yawkat
 */
class ResourceSet extends AbstractResource {
    private final Set<Resource> items;

    public ResourceSet() {
        this(true);
    }

    public ResourceSet(boolean weak) {
        if (weak) {
            this.items = Collections.newSetFromMap(new WeakHashMap<>());
        } else {
            this.items = new HashSet<>();
        }
    }

    public synchronized void register(Resource resource) {
        items.add(resource);
    }

    @Override
    public synchronized void close() {
        items.forEach(Resource::close);
        items.clear();
    }
}
