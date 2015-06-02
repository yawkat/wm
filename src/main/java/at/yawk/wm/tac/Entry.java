package at.yawk.wm.tac;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yawkat
 */
public abstract class Entry {
    private static final EntryState EMPTY_ENTRY_STATE = new EntryState("", false, false);

    @Setter @Getter EntryState state = EMPTY_ENTRY_STATE;
    @Setter boolean selectable = true;

    public void setSelected(boolean selected) {
        setState(state.withSelected(selected));
    }

    public void onUsed() {}
}
