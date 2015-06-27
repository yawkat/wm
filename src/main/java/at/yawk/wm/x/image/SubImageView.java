package at.yawk.wm.x.image;

/**
 * @author yawkat
 */
public class SubImageView extends LocalImage {
    private static final LocalImageType<SubImageView> TYPE = (width, height) -> {
        throw new UnsupportedOperationException();
    };

    private final LocalImage delegate;
    private final int x;
    private final int y;

    public SubImageView(LocalImage delegate, int x, int y, int width, int height) {
        super(width, height);
        this.delegate = delegate;
        this.x = x;
        this.y = y;
    }

    @Override
    public byte getR(int x, int y) {
        return delegate.getR(x + this.x, y + this.y);
    }

    @Override
    public byte getG(int x, int y) {
        return delegate.getG(x + this.x, y + this.y);
    }

    @Override
    public byte getB(int x, int y) {
        return delegate.getB(x + this.x, y + this.y);
    }

    @Override
    public int getRgb(int x, int y) {
        return delegate.getRgb(x + this.x, y + this.y);
    }

    @Override
    public void setR(int x, int y, byte r) {
        delegate.setR(x + this.x, y + this.y, r);
    }

    @Override
    public void setG(int x, int y, byte g) {
        delegate.setG(x + this.x, y + this.y, g);
    }

    @Override
    public void setB(int x, int y, byte b) {
        delegate.setB(x + this.x, y + this.y, b);
    }

    @Override
    public void setRgb(int x, int y, int rgb) {
        delegate.setRgb(x + this.x, y + this.y, rgb);
    }

    @Override
    public LocalImageType<?> getType() {
        return TYPE;
    }

    @Override
    public LocalImage copy() {
        // todo
        throw new UnsupportedOperationException();
    }
}
