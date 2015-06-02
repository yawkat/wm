package at.yawk.wm.tac;

import at.yawk.wm.x.event.KeyPressEvent;
import java.util.stream.Stream;
import lombok.Getter;
import sun.awt.X11.XKeySymConstants;

/**
 * @author yawkat
 */
public class TextFieldFeature extends Feature {
    private TextFieldEntry entry = new TextFieldEntry();
    private TacUI ui;
    @Getter private String text = "";

    @Override
    public void onAdd(TacUI ui) {
        this.ui = ui;
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
        if (evt.getSymbol() <= Character.MAX_VALUE / 2) {
            text += (char) evt.getSymbol();
            update0();
        } else if (evt.getSymbol() == XKeySymConstants.XK_BackSpace) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                update0();
            }
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

    private class TextFieldEntry extends Entry {
        { setSelectable(false); }
    }
}