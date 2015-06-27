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

    public void apply(PixelTransformer transformer, int x, int y) {
        setRgb(x, y, transformer.transform(getRgb(x, y)));
    }

    public void apply(PixelTransformer transformer) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                apply(transformer, x, y);
            }
        }
    }

    /**
     * Get this image as the given type. May copy this image or may just return a view of this image.
     */
    @SuppressWarnings("unchecked")
    public <I extends LocalImage> I as(LocalImageType<I> type) {
        if (type == getType()) { return (I) this; }
        I to = type.createImage(getWidth(), getHeight());
        copyTo(to);
        return to;
    }

    /**
     * Paint this image onto the given target image.
     */
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

    /**
     * Return a copy of this image in the given type. Modifications to the returned image will never change the
     * original
     * image and vice-versa.
     */
    public <I extends LocalImage> I copy(LocalImageType<I> copyType) {
        I to = copyType.createImage(getWidth(), getHeight());
        copyTo(to);
        return to;
    }

    /**
     * Create a copy of this image of an arbitrary type.
     */
    public LocalImage copy() {
        return copy(getType());
    }
}
