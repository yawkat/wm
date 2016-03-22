package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public class AbstractCancellable implements Cancellable {
    private boolean cancelled = false;

    @Override
    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
