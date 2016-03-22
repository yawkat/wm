package at.yawk.wm.tac.launcher;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;
import at.yawk.wm.tac.TacUI;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
class LauncherEntry extends Entry {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LauncherEntry.class);
    protected final TacUI ui;
    private final EntryDescriptor descriptor;
    private final ApplicationRunner applicationRunner;

    public LauncherEntry(TacUI ui, EntryDescriptor descriptor, ApplicationRunner applicationRunner) {
        this.ui = ui;
        this.descriptor = descriptor;
        this.applicationRunner = applicationRunner;
        setState(new EntryState(descriptor.getTitle(), !descriptor.isHighPriority(), false));
    }

    @Override
    public void onUsed() {
        ui.close();
        applicationRunner.run(descriptor.getCommand());
    }

    public EntryDescriptor getDescriptor() {
        return this.descriptor;
    }
}
