package at.yawk.wm.progress;

/**
 * @author yawkat
 */
public interface ProgressTask {
    float getProgress();

    boolean isRunning();

    void addChangeListener(Runnable listener);
}
