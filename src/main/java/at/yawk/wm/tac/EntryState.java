package at.yawk.wm.tac;

/**
 * @author yawkat
 */
public class EntryState {
    String text;
    boolean lowPriority;
    boolean selected;

    @java.beans.ConstructorProperties({ "text", "lowPriority", "selected" })
    public EntryState(String text, boolean lowPriority, boolean selected) {
        this.text = text;
        this.lowPriority = lowPriority;
        this.selected = selected;
    }

    public String getText() {
        return this.text;
    }

    public boolean isLowPriority() {
        return this.lowPriority;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof EntryState)) { return false; }
        final EntryState other = (EntryState) o;
        final Object this$text = this.text;
        final Object other$text = other.text;
        if (this$text == null ? other$text != null : !this$text.equals(other$text)) { return false; }
        if (this.lowPriority != other.lowPriority) { return false; }
        if (this.selected != other.selected) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $text = this.text;
        result = result * PRIME + ($text == null ? 0 : $text.hashCode());
        result = result * PRIME + (this.lowPriority ? 79 : 97);
        result = result * PRIME + (this.selected ? 79 : 97);
        return result;
    }

    public String toString() {
        return "at.yawk.wm.tac.EntryState(text=" + this.text + ", lowPriority=" + this.lowPriority + ", selected=" +
               this.selected + ")";
    }

    public EntryState withText(String text) {
        return this.text == text ? this : new EntryState(text, this.lowPriority, this.selected);
    }

    public EntryState withLowPriority(boolean lowPriority) {
        return this.lowPriority == lowPriority ? this : new EntryState(this.text, lowPriority, this.selected);
    }

    public EntryState withSelected(boolean selected) {
        return this.selected == selected ? this : new EntryState(this.text, this.lowPriority, selected);
    }
}
