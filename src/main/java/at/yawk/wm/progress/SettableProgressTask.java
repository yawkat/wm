package at.yawk.wm.progress;

/**
 * @author yawkat
 */
public interface SettableProgressTask extends ProgressTask {
    void setProgress(float progress);

    void terminate();
}
