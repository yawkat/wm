package at.yawk.wm.tac.password;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author yawkat
 */
class PasswordEditorWindow extends TextEditorWindow {
    // TODO: adjust title according to save status

    private final PasswordManager manager;
    private final at.yawk.password.model.PasswordEntry entry;
    private boolean needsInsert;

    public PasswordEditorWindow(PasswordManager manager, at.yawk.password.model.PasswordEntry entry,
                                boolean needsInsert) {
        super(manager.config.getPassword(), entry.getValue());
        this.manager = manager;
        this.entry = entry;
        this.needsInsert = needsInsert;

        getField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    save();
                    e.consume();
                }
            }
        });
    }

    private void save() {
        System.out.println("Saving password DB");
        entry.setValue(getText());
        if (needsInsert) {
            manager.holder.getPasswords().getPasswords().add(entry);
            needsInsert = false;
        }
        try {
            manager.holder.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}