package at.yawk.wm;

import java.lang.invoke.MethodHandle;

/**
 * Helper java class for {@code MethodHandle.invoke}
 *
 * @author yawkat
 */
class PeriodBuilderInvoker {
    private PeriodBuilderInvoker() {}

    static void invokeVoid(MethodHandle handle) throws Throwable {
        handle.invokeExact();
    }
}
