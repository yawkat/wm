package at.yawk.wm.style;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author yawkat
 */
public class NamedFontDescriptor implements FontDescriptor {
    final String id;

    @JsonCreator
    public NamedFontDescriptor(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof NamedFontDescriptor)) { return false; }
        final NamedFontDescriptor other = (NamedFontDescriptor) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$id = this.id;
        final Object other$id = other.id;
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.id;
        result = result * PRIME + ($id == null ? 0 : $id.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof NamedFontDescriptor;
    }

    public String toString() {
        return "at.yawk.wm.style.NamedFontDescriptor(id=" + this.id + ")";
    }
}
