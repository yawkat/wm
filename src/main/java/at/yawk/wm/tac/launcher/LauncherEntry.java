package at.yawk.wm.tac.launcher;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;

/**
 * @author yawkat
 */
class LauncherEntry extends Entry {
    private final String command;

    public LauncherEntry(String command, String title, boolean highPriority) {
        this.command = command;
        setState(new EntryState(title, !highPriority, false));
    }
}
