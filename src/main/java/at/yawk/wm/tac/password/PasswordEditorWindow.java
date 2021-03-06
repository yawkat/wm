package at.yawk.wm.tac.password;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
class PasswordEditorWindow extends TextEditorWindow {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PasswordEditorWindow.class);
    private final PasswordManager manager;
    private final at.yawk.password.model.PasswordEntry entry;
    private final PasswordHolder.HolderClaim claim;
    private boolean needsInsert;

    public PasswordEditorWindow(PasswordManager manager, at.yawk.password.model.PasswordEntry entry,
                                boolean needsInsert) {
        super(entry.getValue());
        this.manager = manager;
        this.entry = entry;
        this.claim = manager.holder.claim();
        this.needsInsert = needsInsert;

        if (this.claim == null) { throw new IllegalStateException(); }

        getField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    save();
                    e.consume();
                }
                updateTitle();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        boolean modified = !entry.getValue().equals(getText());
        setTitle(entry.getName() + (modified ? "*" : ""));
    }

    @Override
    public void close() {
        super.close();
        claim.unclaim();
    }

    private void save() {
        entry.setValue(getText());
        if (needsInsert) {
            manager.holder.getPasswords().getPasswords().add(entry);
            needsInsert = false;
        }
        try {
            manager.holder.save();
        } catch (Exception e) {
            log.error("Failed to save password database", e);
        }
    }
}
