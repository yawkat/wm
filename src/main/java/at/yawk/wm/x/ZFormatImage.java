package at.yawk.wm.x;

import at.yawk.wm.x.image.LocalImage;
import at.yawk.wm.x.image.LocalImageType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.Getter;

/**
 * @author yawkat
 */
@Getter
public class ZFormatImage extends LocalImage {
    public static final LocalImageType<ZFormatImage> TYPE = (width, height) ->
            new ZFormatImage(width, height,
                             ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()));

    private final ByteBuffer buffer;

    public ZFormatImage(int width, int height, ByteBuffer buffer) {
        super(width, height);

        if (!buffer.isDirect()) { throw new IllegalArgumentException("Buffer must be direct"); }
        this.buffer = buffer;
    }

    private int baseOffset(int x, int y) {
        return (x + y * getWidth()) * 4;
    }

    @Override
    public LocalImageType<? extends ZFormatImage> getType() {
        return TYPE;
    }

    @Override
    public byte getR(int x, int y) {
        return buffer.get(baseOffset(x, y) + 2);
    }

    @Override
    public byte getG(int x, int y) {
        return buffer.get(baseOffset(x, y) + 1);
    }

    @Override
    public byte getB(int x, int y) {
        return buffer.get(baseOffset(x, y));
    }

    @Override
    public void setR(int x, int y, byte r) {
        buffer.put(baseOffset(x, y) + 2, r);
    }

    @Override
    public void setG(int x, int y, byte g) {
        buffer.put(baseOffset(x, y) + 1, g);
    }

    @Override
    public void setB(int x, int y, byte b) {
        buffer.put(baseOffset(x, y), b);
    }
}
