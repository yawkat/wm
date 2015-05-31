package at.yawk.wm.dock.module.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class MovingAverage {
    private final double base;
    private long lastTime = 0;
    @Getter private double average;

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
}
