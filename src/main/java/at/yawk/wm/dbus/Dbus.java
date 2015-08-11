package at.yawk.wm.dbus;

import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class Dbus {
    private static final String DBUS_EXECUTABLE = "dbus-send";

    @Provides
    MediaPlayer mediaPlayer() {
        return implement(MediaPlayer.class);
    }

    @SuppressWarnings("unchecked")
    private <I> I implement(Class<I> itf) {
        Map<Method, Function<Object[], Object>> implementations = new HashMap<>();
        for (Method method : itf.getMethods()) {
            implementations.put(method, implement(method));
        }
        return (I) Proxy.newProxyInstance(
                Dbus.class.getClassLoader(),
                new Class[]{ itf },
                (proxy, method, args) -> implementations.get(method).apply(args)
        );
    }

    private Function<Object[], Object> implement(Method method) {
        String destination = getAnnotation(method, Destination.class).value();
        String objectPath = getAnnotation(method, ObjectPath.class).value();
        List<String> baseList;

        String interfaceName = getAnnotation(method, Interface.class).value();

        DbusMethod dbusMethod = findAnnotation(method, DbusMethod.class);
        if (dbusMethod != null) {
            baseList = Arrays.asList(
                    DBUS_EXECUTABLE,
                    "--type=method_call",
                    "--print-reply=literal",
                    "--dest=" + destination,
                    objectPath,
                    interfaceName + '.' + dbusMethod.value()
            );
        } else {
            DbusSignal dbusSignal = findAnnotation(method, DbusSignal.class);
            if (dbusSignal != null) {
                baseList = Arrays.asList(
                        DBUS_EXECUTABLE,
                        "--type=signal",
                        "--print-reply=literal",
                        "--dest=" + destination,
                        objectPath,
                        interfaceName + '.' + dbusSignal.value()
                );
            } else {
                String property = getAnnotation(method, DbusProperty.class).value();
                baseList = Arrays.asList(
                        DBUS_EXECUTABLE,
                        "--type=method_call",
                        "--print-reply=literal",
                        "--dest=" + destination,
                        objectPath,
                        "org.freedesktop.DBus.Properties.Get",
                        "string:" + interfaceName,
                        "string:" + property
                );
            }
        }

        return args -> {
            List<String> dbus;
            if (args != null && args.length > 0) {
                dbus = new ArrayList<>(baseList.size() + args.length);
                for (Object arg : args) {
                    dbus.add(mapArgument(arg));
                }
            } else {
                dbus = baseList;
            }
            return call(dbus);
        };
    }

    private static <A extends Annotation> A getAnnotation(Method method, Class<A> type) {
        A annotation = findAnnotation(method, type);
        if (annotation == null) {
            throw new NoSuchElementException("Missing annotation of type " + type.getName() + " on " + method);
        }
        return annotation;
    }

    @Nullable
    private static <A extends Annotation> A findAnnotation(Method method, Class<A> type) {
        A annotation = method.getAnnotation(type);
        if (annotation == null) {
            return method.getDeclaringClass().getAnnotation(type);
        } else {
            return annotation;
        }
    }

    private static String mapArgument(Object arg) {
        if (arg instanceof String) {
            return "string:" + arg;
        } else if (arg instanceof Byte) {
            return "byte:" + arg;
        } else if (arg instanceof Short) {
            return "int16:" + arg;
        } else if (arg instanceof Integer) {
            return "int32:" + arg;
        } else if (arg instanceof Long) {
            return "int64:" + arg;
        } else if (arg instanceof Double) {
            return "double:" + arg;
        } else if (arg instanceof Boolean) {
            return "boolean:" + arg;
        } else {
            throw new UnsupportedOperationException("Unsupported argument type " + arg.getClass().getName());
        }
    }

    private String call(List<String> args) {
        if (log.isDebugEnabled()) { log.debug("Calling {}", String.join(" ", args)); }
        try {
            Process process = new ProcessBuilder(args)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start();

            int status = process.waitFor();
            try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = err.readLine()) != null) {
                    log.warn("[DBus] {}", line);
                }
            }

            if (status != 0) { throw new RuntimeException("Status " + status); }

            StringBuilder reply = new StringBuilder();
            try (Reader reader = new InputStreamReader(process.getInputStream())) {
                char[] buf = new char[1024];
                int len;
                while ((len = reader.read(buf)) != -1) {
                    reply.append(buf, 0, len);
                }
            }
            if (reply.length() == 0) { return null; }
            if (!reply.substring(0, 17).equals("   variant       ")) {
                throw new UnsupportedOperationException("Unsupported response '" + reply + "'");
            }
            return reply.substring(17);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

