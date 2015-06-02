package at.yawk.wm.tac.launcher;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;

/**
 * @author yawkat
 */
class ReplLineEntry extends Entry {
    public ReplLineEntry(String text) {
        setState(new EntryState(text, false, false));
        setSelectable(false);
    }
}
