package at.yawk.wm.x.font;

import at.yawk.wm.style.FontStyle;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@ToString(of = "style")
public class GlyphFont {
    private static final char GLYPH_FILE_LENGTH = 256;

    @Getter private final FontStyle style;
    @Nullable private final Path cacheRoot;

    private byte cellWidth;
    private byte cellHeight;

    private final GlyphFile[] glyphFiles = new GlyphFile[(Character.MAX_VALUE + 1) / GLYPH_FILE_LENGTH];

    private GlyphFileFactory glyphFileFactory = null;

    public GlyphFont(FontStyle style, @Nullable Path cacheRoot) {
        this.style = style;
        this.cacheRoot = cacheRoot;
        this.cellWidth = this.cellHeight = (byte) style.getFamily().getCellSize(style);
    }

    /**
     * Create an uncached glyph style.
     */
    public GlyphFont(FontStyle font) {
        this(font, null);
    }

    @Nullable
    private Path getCacheFolder() {
        if (cacheRoot == null) { return null; }
        return cacheRoot.resolve(
                style.getDescriptor() + "-" + cellWidth + "x" + cellHeight
        );
    }

    GlyphFile resolveGlyphFile(char c) {
        GlyphFile loaded = glyphFiles[c / GLYPH_FILE_LENGTH];
        if (loaded != null) { return loaded; }
        return loadGlyphFile(c);
    }

    private synchronized GlyphFile loadGlyphFile(char c) {
        GlyphFile loaded = glyphFiles[c / GLYPH_FILE_LENGTH];
        if (loaded != null) { return loaded; }
        char startInclusive = (char) (c / GLYPH_FILE_LENGTH * GLYPH_FILE_LENGTH);
        char endInclusive = (char) (startInclusive + GLYPH_FILE_LENGTH - 1);
        loaded = tryLoadCachedGlyphFile(startInclusive, endInclusive);
        if (loaded == null) {
            // need to generate
            if (glyphFileFactory == null) {
                glyphFileFactory = new GlyphFileFactory(cellWidth, cellHeight, style);
            }
            loaded = glyphFileFactory.renderRange(startInclusive, endInclusive);

            Path cacheFolder = getCacheFolder();
            if (cacheFolder != null) {
                try {
                    if (!Files.exists(cacheFolder)) {
                        Files.createDirectories(cacheFolder);
                    }
                    Files.write(getCacheFile(cacheFolder, startInclusive), loaded.getData());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        for (int i = startInclusive / GLYPH_FILE_LENGTH; i <= endInclusive / GLYPH_FILE_LENGTH; i++) {
            glyphFiles[i] = loaded;
        }
        return loaded;
    }

    private GlyphFile tryLoadCachedGlyphFile(char startInclusive, char endInclusive) {
        Path cacheFolder = getCacheFolder();
        if (cacheFolder == null) { return null; }
        Path cacheFile = getCacheFile(cacheFolder, startInclusive);
        if (!Files.exists(cacheFile)) { return null; }
        GlyphFile file = new GlyphFile();
        file.setCellWidth(cellWidth);
        file.setCellHeight(cellHeight);
        file.setStartInclusive(startInclusive);
        file.setEndInclusive(endInclusive);
        try {
            file.setData(Files.readAllBytes(cacheFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }

    private static Path getCacheFile(Path cacheFolder, int startInclusive) {
        return cacheFolder.resolve(String.format("%04x", startInclusive));
    }

    public Dimension getStringBounds(CharSequence s) {
        int width = 0;
        int height = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            GlyphFile glyphFile = resolveGlyphFile(c);
            width += glyphFile.getWidth(c);
            height = Math.max(height, glyphFile.getHeight(c));
        }
        return new Dimension(width, height);
    }
}
