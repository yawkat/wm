package at.yawk.wm.tac.password;

import at.yawk.wm.tac.Entry;
import at.yawk.wm.tac.EntryState;

/**
 * @author yawkat
 */
class PasswordEntry extends Entry {
    private final PasswordManager.Instance instance;
    private final String name;

    PasswordEntry(PasswordManager.Instance instance, String name) {
        this.instance = instance;
        this.name = name;

        setState(new EntryState("  " + name, false, false));
    }

    @Override
    public void onUsed() {
        switch (instance.action) {
        case COPY:
            // todo
            break;
        case VIEW:
            new PasswordEditorWindow(
                    instance.getManager(),
                    instance.getManager().holder.getPasswords().getPasswords().stream()
                            .filter(e -> e.getName().equals(name))
                            .findAny().get(),
                    false
            ).show();
            break;
        default:
            throw new IllegalStateException();
        }
    }
}
