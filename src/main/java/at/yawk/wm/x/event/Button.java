package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public enum Button {
    LEFT(1),
    MIDDLE(2),
    RIGHT(3),
    SCROLL_UP(4),
    SCROLL_DOWN(5);

    final int id;

    @java.beans.ConstructorProperties({ "id" })
    private Button(int id) {
        this.id = id;
    }
}
