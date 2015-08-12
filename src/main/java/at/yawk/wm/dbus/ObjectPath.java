package at.yawk.wm.dbus;

import java.lang.annotation.*;

/**
 * @author yawkat
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ObjectPath {
    String value();

    Bus bus() default Bus.USER;
}