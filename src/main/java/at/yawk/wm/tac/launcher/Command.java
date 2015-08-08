package at.yawk.wm.tac.launcher;

import lombok.Data;

/**
 * @author yawkat
 */
@Data
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
}
