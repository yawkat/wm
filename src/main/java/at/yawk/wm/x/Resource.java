package at.yawk.wm.x;

/**
 * @author yawkat
 */
public interface Resource extends AutoCloseable {
    @Override
    void close();
}
