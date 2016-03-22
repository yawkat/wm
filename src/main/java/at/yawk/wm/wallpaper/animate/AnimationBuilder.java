package at.yawk.wm.wallpaper.animate;

import at.yawk.wm.Util;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
class AnimationBuilder {
    private static final Frame EMPTY_FRAME = new Frame();

    static {
        EMPTY_FRAME.setX(0);
        EMPTY_FRAME.setY(0);
        EMPTY_FRAME.setWidth(0);
        EMPTY_FRAME.setHeight(0);
        EMPTY_FRAME.setData(new byte[0]);
    }

    private static class ImageHolder {
        long time;
        BufferedImage image;

        @java.beans.ConstructorProperties({ "time", "image" })
        public ImageHolder(long time, BufferedImage image) {
            this.time = time;
            this.image = image;
        }

        public long getTime() {
            return this.time;
        }

        public BufferedImage getImage() {
            return this.image;
        }

        public boolean equals(Object o) {
            if (o == this) { return true; }
            if (!(o instanceof ImageHolder)) { return false; }
            final ImageHolder other = (ImageHolder) o;
            if (this.time != other.time) { return false; }
            final Object this$image = this.image;
            final Object other$image = other.image;
            if (this$image == null ? other$image != null : !this$image.equals(other$image)) { return false; }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final long $time = this.time;
            result = result * PRIME + (int) ($time >>> 32 ^ $time);
            final Object $image = this.image;
            result = result * PRIME + ($image == null ? 0 : $image.hashCode());
            return result;
        }

        public String toString() {
            return "at.yawk.wm.wallpaper.animate.AnimationBuilder.ImageHolder(time=" + this.time + ", image=" +
                   this.image +
                   ")";
        }
    }

    /**
     * This method loads animations from a directory of the following format:
     *
     * <pre>
     * base.png
     * start100.png
     * start200.png
     * start300.png
     * [...]
     * stop100.png
     * stop200.png
     * stop300.png
     * [...]
     * </pre>
     *
     * The number represents the milliseconds when to show the frame. Every image must have the same size.
     */
    public static AnimatedWallpaper loadDirectory(Path animationDirectory) throws IOException {
        BufferedImage base = Util.INSTANCE.loadImage(animationDirectory.resolve("base.png"));

        AnimatedWallpaper wallpaper = new AnimatedWallpaper();
        wallpaper.setBaseFrame(createFrame(base, null, 0, 0, base.getWidth(), base.getHeight()));

        List<ImageHolder> start = new ArrayList<>();
        List<ImageHolder> stop = new ArrayList<>();

        Pattern framePattern = Pattern.compile("(start|stop)(\\d+)\\.png");

        Stream<Path> files = Files.list(animationDirectory);
        for (Path path : (Iterable<Path>) files::iterator) {
            Matcher matcher = framePattern.matcher(path.getFileName().toString());
            if (matcher.matches()) {
                long time = Long.parseUnsignedLong(matcher.group(2));
                ImageHolder holder = new ImageHolder(time, Util.INSTANCE.loadImage(path));
                (matcher.group(1).equals("start") ? start : stop).add(holder);
            }
        }

        wallpaper.setStart(linkAnimations(wallpaper.getBaseFrame(), start));
        wallpaper.setStop(linkAnimations(wallpaper.getBaseFrame(), stop));

        return wallpaper;
    }

    private static FrameAnimation linkAnimations(Frame background, List<ImageHolder> images) {
        FrameAnimation animation = new FrameAnimation();
        animation.setFrames(new ArrayList<>());

        // sort by time
        images.sort(Comparator.comparingLong(ImageHolder::getTime));

        for (ImageHolder image : images) {
            if (image.time == 0) { continue; }
            long interval = animation.getInterval();
            animation.setInterval(interval == 0 ? image.time : gcd(interval, image.getTime()));
        }

        Frame screen = new Frame(background);
        for (ImageHolder image : images) {
            Frame diff = diffFrame(screen, image.getImage());

            if (!diff.isEmpty()) {
                int i = (int) (image.time / animation.getInterval());
                // fill up with empty frames
                for (int j = animation.getFrames().size(); j <= i; j++) {
                    animation.getFrames().add(EMPTY_FRAME);
                }

                // add our frame
                animation.getFrames().set(i, diff);

                paint(screen, diff);
            }
        }

        return animation;
    }

    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Create a frame that represents the difference of the two input images. Pixel data will come from the front
     * image.
     */
    private static Frame diffFrame(Frame back, BufferedImage front) {
        int minX = back.getWidth();
        int minY = back.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int x = 0; x < back.getWidth(); x++) {
            for (int y = 0; y < back.getHeight(); y++) {
                int f = front.getRGB(x, y);
                if (f >>> 24 != 0) {
                    int bgOffset = (x + y * back.getWidth()) * 3;
                    boolean write = ((f >> 16) & 0xff) != (back.getData()[bgOffset] & 0xff);
                    write |= ((f >> 8) & 0xff) != (back.getData()[bgOffset + 1] & 0xff);
                    write |= (f & 0xff) != (back.getData()[bgOffset + 2] & 0xff);

                    if (write) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }

                } // else {} foreground has 0 alpha, ignore pixel
            }
        }

        if (maxX < minX || maxY < minY) {
            // no pixels to copy
            return EMPTY_FRAME;
        } else {
            return createFrame(front, back, minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
    }

    private static Frame createFrame(BufferedImage image, @Nullable Frame bg,
                                     int startX, int startY, int width, int height) {
        byte[] data = new byte[width * height * 3];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = image.getRGB(x + startX, y + startY);

                int a = rgba >>> 24 & 0xff;
                int r = rgba >>> 16 & 0xff;
                int g = rgba >>> 8 & 0xff;
                int b = rgba & 0xff;

                if (a != 0xff) {
                    double weightFront = a / 0xffD;
                    double weightBack = 1 - weightFront;

                    int backR = 0;
                    int backG = 0;
                    int backB = 0;
                    if (bg != null) {
                        int bgOffset = ((x + startX) + (y + startY) * bg.getWidth()) * 3;
                        backR = bg.getData()[bgOffset] & 0xff;
                        backG = bg.getData()[bgOffset + 1] & 0xff;
                        backB = bg.getData()[bgOffset + 2] & 0xff;
                    }

                    r = (int) (r * weightFront + backR * weightBack);
                    g = (int) (g * weightFront + backG * weightBack);
                    b = (int) (b * weightFront + backB * weightBack);
                }

                int offset = (x + y * width) * 3;
                data[offset] = (byte) r;
                data[offset + 1] = (byte) g;
                data[offset + 2] = (byte) b;
            }
        }
        Frame frame = new Frame();
        frame.setX(startX);
        frame.setY(startY);
        frame.setWidth(width);
        frame.setHeight(height);
        frame.setData(data);
        return frame;
    }

    /**
     * Paint the second frame onto the canvas frame, modifying the former.
     */
    private static void paint(Frame canvas, Frame frame) {
        for (int y = 0; y < frame.getHeight(); y++) {
            int frontOffset = (y * frame.getWidth()) * 3;
            int backOffset = ((frame.getX() - canvas.getX()) +
                              (y + frame.getY() - canvas.getY()) * canvas.getWidth()) * 3;
            System.arraycopy(frame.getData(), frontOffset, canvas.getData(), backOffset, 3 * frame.getWidth());
        }
    }
}
