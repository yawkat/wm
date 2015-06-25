package at.yawk.wm.x;

/**
 * @author yawkat
 */
class RootWindow extends Window {
    RootWindow(Screen screen) {
        super(screen, screen.screen.getRoot());
    }

    @Override
    public int getWidth() {
        return screen.getWidth();
    }

    @Override
    public int getHeight() {
        return screen.getHeight();
    }

    @Override
    protected void destroy() {}
}
