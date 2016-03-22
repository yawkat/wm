package at.yawk.wm.x.icon;

import at.yawk.wm.Util;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.*;
import at.yawk.wm.x.image.BufferedLocalImage;
import at.yawk.wm.x.image.ByteArrayImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.wm.x.image.SubImageView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
@Singleton
public class IconManager {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(IconManager.class);
    @Inject IconConfig config;
    @Inject Screen screen;

    private Map<IconDescriptor, Integer> descriptorIndices;

    /**
     * X-offset of individual icons
     */
    private int[] descriptorOffsets;
    private short[] descriptorWidths;
    private short[] descriptorHeights;

    private LocalImage image;
    private Map<ColorPair, PixMap> colorMaps = new ConcurrentHashMap<>();

    @SneakyThrows
    public void load() {
        descriptorIndices = new HashMap<>();
        Iterator<Map.Entry<IconDescriptor, Path>> iterator = config.getIcons().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .iterator();
        int i = 0;
        while (iterator.hasNext()) {
            descriptorIndices.put(iterator.next().getKey(), i++);
        }

        int descriptorCount = config.getIcons().size();
        descriptorOffsets = new int[descriptorCount];
        descriptorWidths = new short[descriptorCount];
        descriptorHeights = new short[descriptorCount];

        // the stitched map image
        ByteArrayImage mapImage;

        int hash = config.getIcons().hashCode(); // lazy
        Path cacheFile = config.getCacheDir().resolve(String.format("%08x", hash));
        if (Files.exists(cacheFile)) {
            log.info("Loading icon cache...");
            ByteBuffer fileBuffer;
            try (SeekableByteChannel channel = Files.newByteChannel(cacheFile)) {
                fileBuffer = ByteBuffer.allocateDirect(Math.toIntExact(channel.size()));
                while (fileBuffer.hasRemaining()) {
                    channel.read(fileBuffer);
                }
            }
            fileBuffer.flip();

            fileBuffer.position(0);
            fileBuffer.asIntBuffer().get(descriptorOffsets);
            fileBuffer.position(fileBuffer.position() + descriptorCount * 4);
            fileBuffer.asShortBuffer().get(descriptorWidths);
            fileBuffer.position(fileBuffer.position() + descriptorCount * 2);
            fileBuffer.asShortBuffer().get(descriptorHeights);
            fileBuffer.position(fileBuffer.position() + descriptorCount * 2);
            byte[] pixelData = new byte[fileBuffer.remaining()];
            fileBuffer.get(pixelData);
            int mapWidth = 0;
            for (short width : descriptorWidths) {
                mapWidth += width;
            }
            int mapHeight = 0;
            for (short height : descriptorHeights) {
                mapHeight = Math.max(mapHeight, height);
            }
            mapImage = new ByteArrayImage(mapWidth, mapHeight, pixelData, 0, 3);
        } else {
            log.info("Generating icon cache...");
            LocalImage[] images = new LocalImage[descriptorCount];
            for (Map.Entry<IconDescriptor, Path> entry : config.getIcons().entrySet()) {
                int index = descriptorIndices.get(entry.getKey());
                BufferedImage alphaImage = Util.INSTANCE.loadImage(entry.getValue());
                BufferedImage maskImage = new BufferedImage(alphaImage.getWidth(),
                                                            alphaImage.getHeight(),
                                                            BufferedImage.TYPE_3BYTE_BGR);
                maskImage.createGraphics().drawImage(alphaImage, 0, 0, null);
                images[index] = new BufferedLocalImage(maskImage);
            }

            int mapWidth = Arrays.stream(images).mapToInt(LocalImage::getWidth).sum();
            int mapHeight = Arrays.stream(images).mapToInt(LocalImage::getHeight).max().getAsInt();
            mapImage = ByteArrayImage.TYPE.createImage(mapWidth, mapHeight);

            int x = 0;
            for (int j = 0; j < images.length; j++) {
                LocalImage sourceImage = images[j];
                SubImageView targetView = new SubImageView(mapImage,
                                                           x,
                                                           0,
                                                           sourceImage.getWidth(),
                                                           sourceImage.getHeight());
                sourceImage.copyTo(targetView);

                descriptorOffsets[j] = x;
                descriptorWidths[j] = (short) sourceImage.getWidth();
                descriptorHeights[j] = (short) sourceImage.getHeight();

                x += sourceImage.getWidth();
            }

            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(mapImage.getBytes().length + descriptorCount * 8);
            outputBuffer.position(0);
            outputBuffer.asIntBuffer().put(descriptorOffsets);
            outputBuffer.position(outputBuffer.position() + descriptorCount * 4);
            outputBuffer.asShortBuffer().put(descriptorWidths);
            outputBuffer.position(outputBuffer.position() + descriptorCount * 2);
            outputBuffer.asShortBuffer().put(descriptorHeights);
            outputBuffer.position(outputBuffer.position() + descriptorCount * 2);
            outputBuffer.put(mapImage.getBytes());

            outputBuffer.position(0);
            if (!Files.exists(cacheFile.getParent())) {
                Files.createDirectories(cacheFile.getParent());
            }
            try (SeekableByteChannel channel = Files.newByteChannel(
                    cacheFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                while (outputBuffer.hasRemaining()) {
                    channel.write(outputBuffer);
                }
            }
        }

        image = mapImage.as(ZFormatImage.TYPE);
    }

    @Nullable
    public Icon getIconOrNull(@Nullable IconDescriptor descriptor) {
        if (descriptor == null) { return null; }
        return getIcon(descriptor);
    }

    private PixMap getPixMap(Color foreground, Color background) {
        return colorMaps.computeIfAbsent(new ColorPair(foreground, background), p -> {
            LocalImage colorized;

            if (!foreground.equals(Color.WHITE) || !background.equals(Color.BLACK)) {
                colorized = image.copy();
                colorized.apply(new ColorizingPixelTransformer(foreground, background));
            } else {
                colorized = image;
            }

            PixMap map = screen.getRootWindow().createPixMap(colorized.getWidth(), colorized.getHeight());
            try (Graphics graphics = map.createGraphics()) {
                graphics.putImage(0, 0, colorized);
            }
            return map;
        });
    }

    public Icon getIcon(IconDescriptor descriptor) {
        Integer index = descriptorIndices.get(descriptor);
        if (index == null) { throw new NoSuchElementException(descriptor.getId()); }
        return new IconImpl(index);
    }

    private static final class ColorPair {
        private final Color foreground;
        private final Color background;

        @java.beans.ConstructorProperties({ "foreground", "background" })
        public ColorPair(Color foreground, Color background) {
            this.foreground = foreground;
            this.background = background;
        }

        public Color getForeground() {
            return this.foreground;
        }

        public Color getBackground() {
            return this.background;
        }

        public boolean equals(Object o) {
            if (o == this) { return true; }
            if (!(o instanceof ColorPair)) { return false; }
            final ColorPair other = (ColorPair) o;
            final Object this$foreground = this.foreground;
            final Object other$foreground = other.foreground;
            if (this$foreground == null ? other$foreground != null : !this$foreground.equals(other$foreground)) {
                return false;
            }
            final Object this$background = this.background;
            final Object other$background = other.background;
            if (this$background == null ? other$background != null : !this$background.equals(other$background)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $foreground = this.foreground;
            result = result * PRIME + ($foreground == null ? 0 : $foreground.hashCode());
            final Object $background = this.background;
            result = result * PRIME + ($background == null ? 0 : $background.hashCode());
            return result;
        }

        public String toString() {
            return "at.yawk.wm.x.icon.IconManager.ColorPair(foreground=" + this.foreground + ", background=" +
                   this.background + ")";
        }
    }

    private class IconImpl implements Icon {
        private final int xOffset;
        private final short width;
        private final short height;

        public IconImpl(int index) {
            width = descriptorWidths[index];
            height = descriptorHeights[index];
            xOffset = descriptorOffsets[index];
        }

        @Override
        public PixMapArea colorize(Color foreground, Color background) {
            return getPixMap(foreground, background).getArea(xOffset, 0, width, height);
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        public boolean equals(Object o) {
            if (o == this) { return true; }
            if (!(o instanceof IconImpl)) { return false; }
            final IconImpl other = (IconImpl) o;
            if (!other.canEqual((Object) this)) { return false; }
            if (this.xOffset != other.xOffset) { return false; }
            if (this.getWidth() != other.getWidth()) { return false; }
            if (this.getHeight() != other.getHeight()) { return false; }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.xOffset;
            result = result * PRIME + this.getWidth();
            result = result * PRIME + this.getHeight();
            return result;
        }

        protected boolean canEqual(Object other) {
            return other instanceof IconImpl;
        }
    }
}
