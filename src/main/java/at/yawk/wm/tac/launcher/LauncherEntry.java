package at.yawk.wm.tac.launcher;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;
import at.yawk.wm.tac.TacUI;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class LauncherEntry extends Entry {
    protected final TacUI ui;
    @Getter private final EntryDescriptor descriptor;

    public LauncherEntry(TacUI ui, EntryDescriptor descriptor) {
        this.ui = ui;
        this.descriptor = descriptor;
        setState(new EntryState("  " + descriptor.getTitle(), !descriptor.isHighPriority(), false));
    }

    @Override
    public void onUsed() {
        ui.close();
        log.info("Executing command '{}'", descriptor.getCommand());
        try {
            new ProcessBuilder("bash", "-c", descriptor.getCommand())
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start();
        } catch (IOException e) {
            log.warn("Failed to execute command", e);
        }
    }
}
