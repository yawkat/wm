package at.yawk.wm.dbus;

import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class Dbus {
    private final DbusCaller caller = new DbusCaller();
    private final DEventBus eventBus = new DEventBus();

    @Provides
    MediaPlayer mediaPlayer() {
        return implement(MediaPlayer.class);
    }

    @Provides
    NetworkManager networkManager() {
        return implement(NetworkManager.class);
    }

    @Provides
    Power power() {
        return implement(Power.class);
    }

    @PostConstruct
    void startListeners() {
        caller.startListener(Bus.SYSTEM, eventBus);
        caller.startListener(Bus.USER, eventBus);
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
        Destination destination = getAnnotation(method, Destination.class);
        ObjectPath objectPathAnnotation = getAnnotation(method, ObjectPath.class);
        String objectPath = objectPathAnnotation.value();
        String interfaceName = getAnnotation(method, Interface.class).value();
        Bus bus = objectPathAnnotation.bus();

        DbusSignal signal = findAnnotation(method, DbusSignal.class);
        if (signal != null) {
            return args -> {
                Runnable listener = (Runnable) args[0];
                eventBus.subscribe(new DEventBus.EndPoint(bus, objectPath, interfaceName, signal.value()), listener);
                return null;
            };
        }

        List<String> baseList = new ArrayList<>();
        baseList.add("nice");
        baseList.add("dbus-send");
        baseList.add("--print-reply=literal");
        baseList.add("--dest=" + destination.value());
        Collections.addAll(baseList, bus.flags);


        DbusMethod dbusMethod = findAnnotation(method, DbusMethod.class);
        if (dbusMethod != null) {
            baseList.add("--type=method_call");
            baseList.add(objectPath);
            baseList.add(interfaceName + '.' + dbusMethod.value());
        } else {
            String property = getAnnotation(method, DbusProperty.class).value();
            baseList.add("--type=method_call");
            baseList.add(objectPath);
            baseList.add("org.freedesktop.DBus.Properties.Get");
            baseList.add("string:" + interfaceName);
            baseList.add("string:" + property);
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
            return mapResponse(caller.call(dbus));
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

    private static Object mapResponse(String response) {
        if (response == null) { return null; }

        if (response.startsWith("int16")) {
            return Short.parseShort(getResponseBody(response));
        } else if (response.startsWith("uint16")) {
            return (short) Integer.parseUnsignedInt(getResponseBody(response));
        } else if (response.startsWith("int32")) {
            return Integer.parseInt(getResponseBody(response));
        } else if (response.startsWith("uint32")) {
            return Integer.parseUnsignedInt(getResponseBody(response));
        } else if (response.startsWith("int64")) {
            return Long.parseLong(getResponseBody(response));
        } else if (response.startsWith("uint64")) {
            return Long.parseUnsignedLong(getResponseBody(response));
        } else if (response.startsWith("boolean")) {
            return Boolean.parseBoolean(getResponseBody(response));
        } else if (response.startsWith("double")) {
            return Double.parseDouble(getResponseBody(response));
        } else if (response.startsWith("byte")) {
            return Byte.parseByte(getResponseBody(response));
        } else {
            return response;
        }
    }

    private static String getResponseBody(String response) {
        return response.substring(response.indexOf(' ') + 1, response.length() - 1);
    }

}

