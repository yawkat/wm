package at.yawk.wm.hl;

/**
 * @author yawkat
 */
public class TitleEvent {
    String title;

    @java.beans.ConstructorProperties({ "title" })
    public TitleEvent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof TitleEvent)) { return false; }
        final TitleEvent other = (TitleEvent) o;
        final Object this$title = this.title;
        final Object other$title = other.title;
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $title = this.title;
        result = result * PRIME + ($title == null ? 0 : $title.hashCode());
        return result;
    }

    public String toString() {
        return "at.yawk.wm.hl.TitleEvent(title=" + this.title + ")";
    }

    public interface Handler {
        void handle(TitleEvent event);
    }
}
