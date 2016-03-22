package at.yawk.wm.tac;

/**
 * @author yawkat
 */
public abstract class Entry {
    private static final EntryState EMPTY_ENTRY_STATE = new EntryState("", false, false);

    EntryState state = EMPTY_ENTRY_STATE;
    boolean selectable = true;

    public void setSelected(boolean selected) {
        setState(state.withSelected(selected));
    }

    public void onUsed() {}

    public EntryState getState() {
        return this.state;
    }

    public void setState(EntryState state) {
        this.state = state;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}
