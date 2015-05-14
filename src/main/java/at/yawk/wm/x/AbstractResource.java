package at.yawk.wm.x;

/**
 * @author yawkat
 */
public abstract class AbstractResource implements Resource {
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
