package at.yawk.wm.x.event;

import lombok.Getter;

/**
 * @author yawkat
 */
public class AbstractCancellable implements Cancellable {
    @Getter private boolean cancelled = false;

    @Override
    public void cancel() {
        cancelled = true;
    }
}
