package at.yawk.wm.tac;

import lombok.Setter;

/**
 * @author yawkat
 */
public abstract class Entry {
    private static final EntryState EMPTY_ENTRY_STATE = new EntryState("", false, false);

    @Setter
    EntryState state = EMPTY_ENTRY_STATE;

    public void setSelected(boolean selected) {
        setState(state.withSelected(selected));
    }
}
