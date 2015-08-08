package at.yawk.wm.tac.launcher;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class EntryDescriptor {
    String title;
    Command command;
    boolean highPriority;
}
