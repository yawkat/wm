package at.yawk.wm.dock.module.widget;

/**
 * @author yawkat
 */
class MovingAverage {
    private final double base;
    private long lastTime = 0;
    private double average;

    @java.beans.ConstructorProperties({ "base" })
    public MovingAverage(double base) {
        this.base = base;
    }

    public void offer(double value) {
        long now = System.currentTimeMillis();
        if (lastTime == 0) {
            average = value;
        } else {
            long timeDelta = now - lastTime;
            double remainderDeltaBase = Math.pow(base, timeDelta / 1000.);
            average = average * remainderDeltaBase + value * (1 - remainderDeltaBase);
        }
        lastTime = now;
    }

    public double getAverage() {
        return this.average;
    }
}
