package at.yawk.wm.hl;

/**
 * @author yawkat
 */
public class Tag {
    String id;
    State state;

    public Tag() {
    }

    public String getId() {
        return this.id;
    }

    public State getState() {
        return this.state;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof Tag)) { return false; }
        final Tag other = (Tag) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$id = this.id;
        final Object other$id = other.id;
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) { return false; }
        final Object this$state = this.state;
        final Object other$state = other.state;
        if (this$state == null ? other$state != null : !this$state.equals(other$state)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.id;
        result = result * PRIME + ($id == null ? 0 : $id.hashCode());
        final Object $state = this.state;
        result = result * PRIME + ($state == null ? 0 : $state.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof Tag;
    }

    public String toString() {
        return "at.yawk.wm.hl.Tag(id=" + this.id + ", state=" + this.state + ")";
    }

    public enum State {
        SELECTED,
        RUNNING,
        EMPTY
    }
}
