package at.yawk.wm.x.image;

/**
 * @author yawkat
 */
public interface LocalImageType<I extends LocalImage> {
    I createImage(int width, int height);
}
