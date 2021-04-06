package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import org.freedesktop.xcb.LibXcbConstants;

/**
 * @author yawkat
 */
class CloseFeature extends Feature {
    private TacUI ui;

    @Override
    public void onAdd(TacUI ui) {
        this.ui = ui;
    }

    @Override
    public void onKeyPress(KeyPressEvent evt) {
        if (evt.getSymbol() == LibXcbConstants.XKB_KEY_Escape) {
            ui.close();
            evt.cancel();
        }
    }
}
