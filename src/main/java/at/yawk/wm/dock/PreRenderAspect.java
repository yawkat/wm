package at.yawk.wm.dock;

/**
 * @author yawkat
 */
@FunctionalInterface
interface PreRenderAspect {
    void apply(Widget widget);
}
