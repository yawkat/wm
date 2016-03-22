package at.yawk.wm.tac.launcher;

/**
 * @author yawkat
 */
public class EntryDescriptor {
    String title;
    Command command;
    boolean highPriority;

    @java.beans.ConstructorProperties({ "title", "command", "highPriority" })
    public EntryDescriptor(String title, Command command, boolean highPriority) {
        this.title = title;
        this.command = command;
        this.highPriority = highPriority;
    }

    public String getTitle() {
        return this.title;
    }

    public Command getCommand() {
        return this.command;
    }

    public boolean isHighPriority() {
        return this.highPriority;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof EntryDescriptor)) { return false; }
        final EntryDescriptor other = (EntryDescriptor) o;
        final Object this$title = this.title;
        final Object other$title = other.title;
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) { return false; }
        final Object this$command = this.command;
        final Object other$command = other.command;
        if (this$command == null ? other$command != null : !this$command.equals(other$command)) { return false; }
        if (this.highPriority != other.highPriority) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $title = this.title;
        result = result * PRIME + ($title == null ? 0 : $title.hashCode());
        final Object $command = this.command;
        result = result * PRIME + ($command == null ? 0 : $command.hashCode());
        result = result * PRIME + (this.highPriority ? 79 : 97);
        return result;
    }

    public String toString() {
        return "at.yawk.wm.tac.launcher.EntryDescriptor(title=" + this.title + ", command=" + this.command +
               ", highPriority=" + this.highPriority + ")";
    }
}
