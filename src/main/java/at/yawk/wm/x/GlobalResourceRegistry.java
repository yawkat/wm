package at.yawk.wm.x;

/**
 * @author yawkat
 */
public class GlobalResourceRegistry {
    private final ResourceSet resources = new ResourceSet(false); // strong refs

    GlobalResourceRegistry() {}

    void close() {
        resources.close();
    }

    public void register(Resource resource) {
        resources.register(resource);
    }
}
