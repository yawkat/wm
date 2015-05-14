package at.yawk.wm.dock.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yawkat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DockWidget {
    Position position() default Position.LEFT;

    enum Position {
        LEFT,
        RIGHT,
    }
}
