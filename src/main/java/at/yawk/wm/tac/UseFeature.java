package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import sun.awt.X11.XKeySymConstants;

/**
 * @author yawkat
 */
public class UseFeature extends Feature {
    private TacUI ui;

    @Override
    public void onAdd(TacUI ui) {
        this.ui = ui;
    }

    @Override
    public void onKeyPress(KeyPressEvent evt) {
        if (evt.getSymbol() == XKeySymConstants.XK_Return) { // enter
            onEnter();
            evt.cancel();
        }
    }

    protected void onEnter() {
        for (Entry entry : ui.getEntries()) {
            if (entry.state.isSelected()) {
                entry.onUsed();
                break;
            }
        }
    }
}
