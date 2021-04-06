package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import org.freedesktop.xcb.LibXcbConstants;

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
        if (evt.getSymbol() == LibXcbConstants.XKB_KEY_Return) { // enter
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
