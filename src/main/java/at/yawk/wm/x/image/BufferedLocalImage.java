package at.yawk.wm.x.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author yawkat
 */
public class BufferedLocalImage extends LocalImage {
    public static final LocalImageType<BufferedLocalImage> TYPE = (width, height) ->
            new BufferedLocalImage(new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR));

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
        return image.getRGB(x, y) & 0xffffff;
    }

    @Override
    public void setRgb(int x, int y, int rgb) {
        image.setRGB(x, y, rgb | 0xff000000);
    }

    public static void saveImage(LocalImage image, File file) throws IOException {
        ImageIO.write(image.as(BufferedLocalImage.TYPE).getImage(), "PNG", file);
    }

    public BufferedImage getImage() {
        return this.image;
    }
}
