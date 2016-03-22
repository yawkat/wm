package at.yawk.wm.tac;

import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Singleton
public class ModalRegistry {
    private Modal currentModal = null;

    public synchronized void onOpen(Modal modal) {
        closeCurrent();
        modal.addCloseListener(() -> {
            if (currentModal == modal) { currentModal = null; }
        });
        currentModal = modal;
    }

    public synchronized boolean closeCurrent() {
        if (currentModal != null) {
            currentModal.close();
            return true;
        } else {
            return false;
        }
    }
}
