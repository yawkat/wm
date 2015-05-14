package at.yawk.wm.dock.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author yawkat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Periodic {
    int value();

    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * If set to true, a render will follow after method execution.
     */
    boolean render() default false;
}
