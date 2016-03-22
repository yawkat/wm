package at.yawk.wm.x.image;

import java.util.Arrays;

/**
 * @author yawkat
 */
public class ByteArrayImage extends LocalImage {
    public static final LocalImageType<ByteArrayImage> TYPE = (width, height) ->
            new ByteArrayImage(width, height, new byte[width * height * 3], 0, 3);

    private final byte[] bytes;
    private final int start;
    private final int pixelOffset;

    public ByteArrayImage(int width, int height, byte[] bytes, int start, int pixelOffset) {
        super(width, height);
        this.bytes = bytes;
        this.start = start;
        this.pixelOffset = pixelOffset;
    }

    private int baseOffset(int x, int y) {
        return start + (x + y * getWidth()) * pixelOffset;
    }

    @Override
    public LocalImageType<? extends ByteArrayImage> getType() {
        return TYPE;
    }

    @Override
    public byte getR(int x, int y) {
        return bytes[baseOffset(x, y)];
    }

    @Override
    public byte getG(int x, int y) {
        return bytes[baseOffset(x, y) + 1];
    }

    @Override
    public byte getB(int x, int y) {
        return bytes[baseOffset(x, y) + 2];
    }

    @Override
    public void setR(int x, int y, byte r) {
        bytes[baseOffset(x, y)] = r;
    }

    @Override
    public void setG(int x, int y, byte g) {
        bytes[baseOffset(x, y) + 1] = g;
    }

    @Override
    public void setB(int x, int y, byte b) {
        bytes[baseOffset(x, y) + 2] = b;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends LocalImage> I copy(LocalImageType<I> copyType) {
        if (copyType == TYPE) {
            int end = start + getWidth() * getHeight() * pixelOffset;
            byte[] copyBytes = start == 0 && end == bytes.length ?
                    bytes.clone() :
                    Arrays.copyOfRange(this.bytes, start, end);

            return (I) new ByteArrayImage(getWidth(), getHeight(), copyBytes, 0, pixelOffset);
        }
        return super.copy(copyType);
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public int getStart() {
        return this.start;
    }

    public int getPixelOffset() {
        return this.pixelOffset;
    }
}
