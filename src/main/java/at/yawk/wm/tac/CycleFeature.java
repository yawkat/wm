package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import java.util.List;
import sun.awt.X11.XKeySymConstants;

/**
 * @author yawkat
 */
public class CycleFeature extends Feature {
    private TacUI ui;
    private List<Entry> lastBeforeSelected;
    private Entry lastSelected;

    @Override
    public void onAdd(TacUI ui) {
        this.ui = ui;
    }

    @Override
    public void onEntriesSet() {
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;
        }

        int selectedI = -1;
        for (int i = 0; i < ui.getEntries().size(); i++) {
            Entry entry = ui.getEntries().get(i);
            if (entry.state.isSelected()) {
                // already have a selected item
                selectedI = i;
                break;
            }
        }
        if (selectedI == -1) {
            if (lastBeforeSelected != null) {
                for (int i = lastBeforeSelected.size() - 1; i >= 0; i--) {
                    Entry e = lastBeforeSelected.get(i);
                    if (e.selectable) {
                        selectedI = ui.getEntries().indexOf(e);
                        if (selectedI != -1) { break; }
                    }
                }
            }
            if (selectedI == -1) {
                for (int i = 0; i < ui.getEntries().size(); i++) {
                    Entry entry = ui.getEntries().get(i);
                    if (entry.selectable) {
                        selectedI = i;
                        break;
                    }
                }
            }
        }

        if (selectedI != -1 && selectedI < ui.getEntries().size()) {
            lastSelected = ui.getEntries().get(selectedI);
            lastSelected.setSelected(true);
            lastBeforeSelected = ui.getEntries().subList(0, selectedI);
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent evt) {
        if (evt.getSymbol() == XKeySymConstants.XK_Up) { // arrow up
            cycle(-1);
            evt.cancel();
        } else if (evt.getSymbol() == XKeySymConstants.XK_Down) { // arrow down
            cycle(+1);
            evt.cancel();
        }
    }

    private void cycle(int delta) {
        if (ui.getEntries().isEmpty()) { return; }
        for (int i = 0; i < ui.getEntries().size(); i++) {
            Entry entry = ui.getEntries().get(i);
            if (entry.state.isSelected()) {
                int sign = delta > 0 ? 1 : -1;
                while (delta != 0) {
                    i = (i + sign + ui.getEntries().size()) % ui.getEntries().size();
                    if (ui.getEntries().get(i).selectable) {
                        delta -= sign;
                    }
                }
                entry.setSelected(false);
                ui.getEntries().get(i).setSelected(true);
                ui.update();
                break;
            }
        }
    }
}
