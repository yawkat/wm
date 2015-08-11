package at.yawk.wm.x.icon;

import at.yawk.wm.Util;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.*;
import at.yawk.wm.x.image.BufferedLocalImage;
import at.yawk.wm.x.image.ByteArrayImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.wm.x.image.SubImageView;
import at.yawk.yarn.Component;
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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@Component
public class IconManager {
    private static final int HEADER_ITEM_LENGTH = 2;

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

    @PostConstruct
    @SneakyThrows
    void load() {
        descriptorIndices = new HashMap<>();
        Iterator<Map.Entry<IconDescriptor, Path>> iterator = config.getIcons().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .iterator();
        int i = 0;
        while (iterator.hasNext()) {
            descriptorIndices.put(iterator.next().getKey(), i);
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
                BufferedImage alphaImage = Util.loadImage(entry.getValue());
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
        Integer indexObject = descriptorIndices.get(descriptor);
        if (indexObject == null) { throw new NoSuchElementException(descriptor.getId()); }
        int index = indexObject;

        short width = descriptorWidths[index];
        short height = descriptorHeights[index];
        int xOffset = descriptorOffsets[index];
        return new Icon() {
            @Override
            public PixMapArea colorize(Color foreground, Color background) {
                return getPixMap(foreground, background).getArea(xOffset, 0, width, height);
            }

            @Override
            public int getWidth() {
                return getPixMap(Color.WHITE, Color.BLACK).getWidth();
            }

            @Override
            public int getHeight() {
                return getPixMap(Color.WHITE, Color.BLACK).getHeight();
            }
        };
    }

    @Value
    private static final class ColorPair {
        private final Color foreground;
        private final Color background;
    }

}
