package at.yawk.wm.ui;

/**
 * @author yawkat
 */
@FunctionalInterface
interface PreRenderAspect {
    void apply(Widget widget);
}
