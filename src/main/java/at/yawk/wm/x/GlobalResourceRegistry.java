package at.yawk.wm.x;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Singleton
public class GlobalResourceRegistry {
    private final ResourceSet resources = new ResourceSet(false); // strong refs

    @Inject
    GlobalResourceRegistry() {}

    void close() {
        resources.close();
    }

    public void register(Resource resource) {
        resources.register(resource);
    }
}
