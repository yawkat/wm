package at.yawk.wm.tac;

import at.yawk.yarn.Component;

/**
 * @author yawkat
 */
@Component
public class ModalRegistry {
    private Modal currentModal = null;

    public void onOpen(Modal modal) {
        closeCurrent();
        modal.addCloseListener(() -> {
            if (currentModal == modal) { currentModal = null; }
        });
    }

    public boolean closeCurrent() {
        if (currentModal != null) {
            currentModal.close();
            return true;
        } else {
            return false;
        }
    }
}
