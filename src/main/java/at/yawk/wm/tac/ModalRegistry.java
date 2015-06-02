package at.yawk.wm.tac;

import at.yawk.yarn.Component;

/**
 * @author yawkat
 */
@Component
public class ModalRegistry {
    private Modal currentModal = null;

    public void onOpen(Modal modal) {
        if (currentModal != null) {
            currentModal.close();
        }
        modal.addCloseListener(() -> {
            if (currentModal == modal) { currentModal = null; }
        });
    }
}
