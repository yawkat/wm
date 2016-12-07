package at.yawk.wm.x;

import at.yawk.wm.x.font.FontRenderer;
import at.yawk.wm.x.font.GlyphFont;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Singleton;
import org.freedesktop.xcb.*;
import org.slf4j.Logger;
import xcb4j.LibXcbLoader;

/**
 * @author yawkat
 */
@Singleton
public class XcbConnector implements Resource {
    static final boolean DEBUG_ERRORS = false;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(XcbConnector.class);

    static { LibXcbLoader.load(); }

    private final GlobalResourceRegistry globalResourceRegistry = new GlobalResourceRegistry();

    SWIGTYPE_p_xcb_connection_t connection;
    xcb_setup_t setup;
    xcb_format_t format;

    private Screen screen;
    private EventManager eventManager;
    private BasicFontRegistry basicFontRegistry;
    private Thread eventThread;
    KeyManager keyManager;

    final Map<GlyphFont, FontRenderer> fontRenderers =
            Collections.synchronizedMap(new WeakHashMap<>());

    public void open() {
        log.info("Connecting to X server...");
        ByteBuffer ptr = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        connection = LibXcb.xcb_connect(null, ptr);
        log.info("Connected to X server");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            globalResourceRegistry.close();
            this.close(); // close us last
        }));

        xcb_setup_t setup = LibXcb.xcb_get_setup(getConnection());
        xcb_screen_iterator_t itr = LibXcb.xcb_setup_roots_iterator(setup);

        int screenId = ptr.getInt();
        for (int i = 0; i < screenId; i++) {
            LibXcb.xcb_screen_next(itr);
        }
        System.out.println("screenId: " + screenId);
        screen = new Screen(this, itr.getData());
        log.debug("Screen: width={} height={} depth={}",
                  screen.screen.getWidth_in_pixels(),
                  screen.screen.getHeight_in_pixels(),
                  screen.screen.getRoot_depth());

        basicFontRegistry = new BasicFontRegistry(this);

        log.info("Starting event handler...");
        eventManager = new EventManager(this);
        eventThread = new Thread(eventManager);
        eventThread.setDaemon(true);
        eventThread.setName("XCB event handler");
        eventThread.start();

        this.setup = LibXcb.xcb_get_setup(connection);
        this.format = LibXcb.xcb_setup_pixmap_formats(setup);

        keyManager = new KeyManager(this);
        globalResourceRegistry.register(keyManager);
        log.info("X setup complete");
    }

    @Override
    public void close() {
        synchronized (fontRenderers) {
            fontRenderers.values().forEach(FontRenderer::close);
            fontRenderers.clear();
        }

        if (connection == null) { return; }
        basicFontRegistry.close();

        LibXcb.xcb_disconnect(connection);
        connection = null;

        if (eventThread != null) {
            eventThread.interrupt();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public SWIGTYPE_p_xcb_connection_t getConnection() {
        return connection;
    }

    public Screen getScreen() {
        return screen;
    }

    public GlobalResourceRegistry globalResourceRegistry() {
        return globalResourceRegistry;
    }

    EventManager getEventManager() {
        return eventManager;
    }

    BasicFontRegistry getBasicFontRegistry() {
        return basicFontRegistry;
    }

    void flush() {
        if (DEBUG_ERRORS) {
            eventManager.lastFlushStackTrace = new Throwable("Flush");
        }
        int flush = LibXcb.xcb_flush(connection);
        if (flush <= 0) {
            int error = LibXcb.xcb_connection_has_error(connection);
            log.error("Failed to flush X: {} ; connection status: {}", flush, error, new Throwable());
        }
    }

    int internAtom(String atom) {
        return internAtoms(atom)[0];
    }

    private final Map<String, Integer> atomCache = new HashMap<>();

    synchronized int[] internAtoms(String... atoms) {
        xcb_intern_atom_cookie_t[] cookies = new xcb_intern_atom_cookie_t[atoms.length];
        int[] ids = new int[atoms.length];
        for (int i = 0; i < atoms.length; i++) {
            Integer present = this.atomCache.get(atoms[i]);
            if (present == null) {
                cookies[i] = LibXcb.xcb_intern_atom(
                        connection,
                        (short) 0,
                        atoms[i].length(),
                        atoms[i]
                );
            } else {
                ids[i] = present;
            }
        }
        for (int i = 0; i < atoms.length; i++) {
            if (cookies[i] != null) {
                ids[i] = LibXcb.xcb_intern_atom_reply(connection, cookies[i], new xcb_generic_error_t(0, false))
                        .getAtom();
                this.atomCache.put(atoms[i], ids[i]);
            }
        }
        return ids;
    }

    public void checkError() {
        if (DEBUG_ERRORS) {
            flush();
        }
    }

    Graphics wrapGraphics(Graphics graphics) {
        if (DEBUG_ERRORS) {
            return (Graphics) Proxy.newProxyInstance(
                    XcbConnector.class.getClassLoader(),
                    new Class[]{ Graphics.class },
                    (proxy, method, args) -> {
                        Object result = method.invoke(graphics, args);
                        if (DEBUG_ERRORS) {
                            graphics.flush();
                        }
                        return result == graphics ? proxy : result;
                    }
            );
        } else {
            return graphics;
        }
    }
}
