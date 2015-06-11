package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public interface Cancellable {
    void cancel();

    boolean isCancelled();
}
