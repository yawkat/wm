package at.yawk.wm.tac;

/**
 * @author yawkat
 */
public interface Modal {
    void close();

    void addCloseListener(Runnable listener);
}
