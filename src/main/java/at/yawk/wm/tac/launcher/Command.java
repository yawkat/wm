package at.yawk.wm.tac.launcher;

/**
 * @author yawkat
 */
public class Command {
    private String[] command;
    private boolean jail = false;

    public Command(String command) {
        this(new String[]{ command });
    }

    public Command(String[] command) {
        setCommand(command);
    }

    public Command() {}

    public String[] getCommand() {
        return this.command;
    }

    public boolean isJail() {
        return this.jail;
    }

    public void setCommand(String[] command) {
        this.command = command;
    }

    public void setJail(boolean jail) {
        this.jail = jail;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof Command)) { return false; }
        final Command other = (Command) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (!java.util.Arrays.deepEquals(this.command, other.command)) { return false; }
        if (this.jail != other.jail) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.deepHashCode(this.command);
        result = result * PRIME + (this.jail ? 79 : 97);
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof Command;
    }

    public String toString() {
        return "at.yawk.wm.tac.launcher.Command(command=" + java.util.Arrays.deepToString(this.command) + ", jail=" +
               this.jail + ")";
    }
}
