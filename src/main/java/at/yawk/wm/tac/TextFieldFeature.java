package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import java.util.stream.Stream;

import org.freedesktop.xcb.LibXcbConstants;

/**
 * @author yawkat
 */
public class TextFieldFeature extends Feature {
    private TextFieldEntry entry = new TextFieldEntry();
    private TacUI ui;
    private String text = "";

    @Override
    public void onAdd(TacUI ui) {
        this.ui = ui;
    }

    public void clear() {
        text = "";
        update0();
    }

    @Override
    public Stream<? extends Entry> setEntries(Stream<? extends Entry> entries, int entryLimit) {
        updateState();
        return Stream.concat(
                Stream.of(entry),
                entries
        );
    }

    @Override
    public void onKeyPress(KeyPressEvent evt) {
        if (evt.getSymbol() == LibXcbConstants.XKB_KEY_BackSpace) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                update0();
            }
            evt.cancel();
        } else if (evt.getKeyChar() != 0) {
            text += (char) evt.getKeyChar();
            update0();
            evt.cancel();
        }
    }

    private void update0() {
        onUpdate();
        updateState();
        ui.update();
    }

    private void updateState() {
        entry.setState(new EntryState(format(text), false, false));
    }

    protected void onUpdate() {}

    protected String format(String text) {
        return text;
    }

    public String getText() {
        return this.text;
    }

    private class TextFieldEntry extends Entry {
        { setSelectable(false); }
    }
}
