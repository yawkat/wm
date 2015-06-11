package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import sun.awt.X11.XKeySymConstants;

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
        if (evt.getSymbol() == XKeySymConstants.XK_Escape) {
            ui.close();
            evt.cancel();
        }
    }
}
