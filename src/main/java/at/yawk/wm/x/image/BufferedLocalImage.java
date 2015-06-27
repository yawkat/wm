package at.yawk.wm.x.image;

import java.awt.image.BufferedImage;
import lombok.Getter;

/**
 * @author yawkat
 */
public class BufferedLocalImage extends LocalImage {
    public static final LocalImageType<BufferedLocalImage> TYPE = (width, height) ->
            new BufferedLocalImage(new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR));

    @Getter
    private final BufferedImage image;

    public BufferedLocalImage(BufferedImage image) {
        super(image.getWidth(), image.getHeight());
        this.image = image;
    }

    @Override
    public LocalImageType<? extends BufferedLocalImage> getType() {
        return TYPE;
    }

    @Override
    public int getRgb(int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public void setRgb(int x, int y, int rgb) {
        image.setRGB(x, y, rgb);
    }
}
