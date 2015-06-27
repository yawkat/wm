package at.yawk.wm.x.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@Getter
@RequiredArgsConstructor
public abstract class LocalImage {
    private final int width;
    private final int height;

    public abstract LocalImageType<?> getType();

    public byte getR(int x, int y) {
        return (byte) ((getRgb(x, y) >>> 16) & 0xff);
    }

    public byte getG(int x, int y) {
        return (byte) ((getRgb(x, y) >>> 8) & 0xff);
    }

    public byte getB(int x, int y) {
        return (byte) (getRgb(x, y) & 0xff);
    }

    public int getRgb(int x, int y) {
        byte r = getR(x, y);
        byte g = getG(x, y);
        byte b = getB(x, y);
        return (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    public void setR(int x, int y, byte r) {
        setRgb(x, y, getRgb(x, y) & ~0xff0000 | (r & 0xff) << 16);
    }

    public void setG(int x, int y, byte g) {
        setRgb(x, y, getRgb(x, y) & ~0x00ff00 | (g & 0xff) << 8);
    }

    public void setB(int x, int y, byte b) {
        setRgb(x, y, getRgb(x, y) & ~0x0000ff | (b & 0xff));
    }

    public void setRgb(int x, int y, int rgb) {
        setR(x, y, (byte) ((rgb >>> 16) & 0xff));
        setG(x, y, (byte) ((rgb >>> 8) & 0xff));
        setB(x, y, (byte) (rgb & 0xff));
    }

    @SuppressWarnings("unchecked")
    public <I extends LocalImage> I convertTo(LocalImageType<I> type) {
        if (type == getType()) { return (I) this; }
        I to = type.createImage(getWidth(), getHeight());
        this.copyTo(to);
        return to;
    }

    public void copyTo(LocalImage to) {
        if (to.getWidth() < this.getWidth() || to.getHeight() < this.getHeight()) {
            throw new IllegalArgumentException("Target image too small");
        }
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                to.setRgb(x, y, getRgb(x, y));
            }
        }
    }
}
