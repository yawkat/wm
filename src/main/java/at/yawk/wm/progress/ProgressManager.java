package at.yawk.wm.progress;

import at.yawk.yarn.Component;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author yawkat
 */
@Component
public class ProgressManager {
    private final Collection<Consumer<ProgressTask>> addListeners = new ConcurrentLinkedQueue<>();

    public SettableProgressTask createTask() {
        SimpleProgressTask task = new SimpleProgressTask();
        addListeners.forEach(l -> l.accept(task));
        return task;
    }

    public void addTaskCreateListener(Consumer<ProgressTask> taskListener) {
        addListeners.add(taskListener);
    }

    class SimpleProgressTask implements SettableProgressTask {
        private final Collection<Runnable> listeners = new ConcurrentLinkedQueue<>();

        private float progress;
        private boolean running = true;

        @Override
        public void setProgress(float progress) {
            if (!running) { throw new IllegalStateException("Already terminated"); }
            this.progress = progress;
            notifyListeners();
        }

        @Override
        public void terminate() {
            this.running = false;
            notifyListeners();
        }

        private void notifyListeners() {
            listeners.forEach(Runnable::run);
        }

        @Override
        public float getProgress() {
            return progress;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void addChangeListener(Runnable listener) {
            listeners.add(listener);
        }
    }
}
