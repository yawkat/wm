package at.yawk.wm.x;

import at.yawk.wm.x.font.FontRenderer;
import at.yawk.wm.x.font.GlyphFont;
import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Singleton;
import org.freedesktop.xcb.*;
import xcb4j.LibXcbLoader;

/**
 * @author yawkat
 */
@Component
public class XcbConnector implements Resource {
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

    public XcbConnector() {
        open();
    }

    private void open() {
        ByteBuffer ptr = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        connection = LibXcb.xcb_connect(null, ptr);
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
        screen = new Screen(this, itr.getData());

        basicFontRegistry = new BasicFontRegistry(this);

        eventManager = new EventManager(this);
        eventThread = new Thread(eventManager);
        eventThread.setDaemon(true);
        eventThread.setName("XCB event handler");
        eventThread.start();

        this.setup = LibXcb.xcb_get_setup(connection);
        this.format = LibXcb.xcb_setup_pixmap_formats(setup);

        keyManager = new KeyManager(this);
        globalResourceRegistry.register(keyManager);
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

    @Provides
    public SWIGTYPE_p_xcb_connection_t getConnection() {
        return connection;
    }

    @Provides
    public Screen getScreen() {
        return screen;
    }

    @Provides
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
        LibXcb.xcb_flush(connection);
    }

    int internAtom(String atom) {
        return internAtoms(atom)[0];
    }

    int[] internAtoms(String... atoms) {
        xcb_intern_atom_cookie_t[] cookies = new xcb_intern_atom_cookie_t[atoms.length];
        for (int i = 0; i < atoms.length; i++) {
            cookies[i] = LibXcb.xcb_intern_atom(
                    connection,
                    (short) 0,
                    atoms[i].length(),
                    atoms[i]
            );
        }
        int[] ids = new int[atoms.length];
        for (int i = 0; i < atoms.length; i++) {
            ids[i] = LibXcb.xcb_intern_atom_reply(connection, cookies[i], new xcb_generic_error_t(0, false))
                    .getAtom();
        }
        return ids;
    }
}
