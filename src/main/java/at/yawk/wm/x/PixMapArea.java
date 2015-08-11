package at.yawk.wm.x;

/**
 * @author yawkat
 */
public interface PixMapArea {
    PixMapArea getArea(int x, int y, int width, int height);

    int getWidth();

    int getHeight();
}
