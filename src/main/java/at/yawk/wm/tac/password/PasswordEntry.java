package at.yawk.wm.tac.password;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author yawkat
 */
class PasswordEntry extends Entry {
    private final PasswordManager.Instance instance;
    private final String name;

    PasswordEntry(PasswordManager.Instance instance, String name) {
        this.instance = instance;
        this.name = name;

        setState(new EntryState(name, false, false));
    }

    @Override
    public void onUsed() {
        switch (instance.action) {
        case COPY:
            String lines = findEntry().getValue();
            int firstLn = lines.indexOf('\n');
            StringSelection selection = new StringSelection(firstLn == -1 ? lines : lines.substring(0, firstLn));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            instance.ui.close();
            break;
        case VIEW:
            new PasswordEditorWindow(instance.getManager(), findEntry(), false).show();
            break;
        default:
            throw new IllegalStateException();
        }
    }

    private at.yawk.password.model.PasswordEntry findEntry() {
        return instance.getManager().holder.getPasswords().getPasswords().stream()
                .filter(e -> e.getName().equals(name))
                .findAny().get();
    }
}
