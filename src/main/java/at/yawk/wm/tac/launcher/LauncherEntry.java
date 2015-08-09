package at.yawk.wm.tac.launcher;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;
import at.yawk.wm.tac.TacUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class LauncherEntry extends Entry {
    protected final TacUI ui;
    @Getter private final EntryDescriptor descriptor;
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
}
