package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public class KeyPressEvent extends AbstractCancellable {
    private int x;
    private int y;
    private int detail;
    private int symbol;
    private char keyChar;

    @java.beans.ConstructorProperties({ "x", "y", "detail", "symbol", "keyChar" })
    public KeyPressEvent(int x, int y, int detail, int symbol, char keyChar) {
        this.x = x;
        this.y = y;
        this.detail = detail;
        this.symbol = symbol;
        this.keyChar = keyChar;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getDetail() {
        return this.detail;
    }

    public int getSymbol() {
        return this.symbol;
    }

    public char getKeyChar() {
        return this.keyChar;
    }

    public String toString() {
        return "at.yawk.wm.x.event.KeyPressEvent(x=" + this.x + ", y=" + this.y + ", detail=" + this.detail +
               ", symbol=" + this.symbol + ", keyChar=" + this.keyChar + ")";
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof KeyPressEvent)) { return false; }
        final KeyPressEvent other = (KeyPressEvent) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (this.getX() != other.getX()) { return false; }
        if (this.getY() != other.getY()) { return false; }
        if (this.getDetail() != other.getDetail()) { return false; }
        if (this.getSymbol() != other.getSymbol()) { return false; }
        if (this.getKeyChar() != other.getKeyChar()) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getDetail();
        result = result * PRIME + this.getSymbol();
        result = result * PRIME + this.getKeyChar();
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof KeyPressEvent;
    }
}
