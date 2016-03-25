package at.yawk.wm.ui;

import java.lang.ref.WeakReference;

/**
 * @author yawkat
 */
class WeakRunnable implements Runnable {
    private final WeakReference<Runnable> handle;

    public WeakRunnable(Runnable handle) {
        this.handle = new WeakReference<>(handle);
    }

    @Override
    public void run() {
        Runnable runnable = handle.get();
        if (runnable != null) {
            runnable.run();
        }
    }
}
