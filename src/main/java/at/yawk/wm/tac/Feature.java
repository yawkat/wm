package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import java.util.stream.Stream;

/**
 * @author yawkat
 */
public abstract class Feature {
    public void onAdd(TacUI ui) {}

    public Stream<? extends Entry> setEntries(Stream<? extends Entry> entries, int entryLimit) {
        return entries;
    }

    public void onEntriesSet() {}

    public void onKeyPress(KeyPressEvent evt) {}
}
