package at.yawk.wm.x.font;

import java.util.Arrays;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
class GlyphFile {
    static final int GLYPH_HEADER_LENGTH = 3;

    /*
     * Glyph file format:
     *
     * 1. HEADER
     *  for each glyph, there are 4 bytes of header data:
     *   - glyph width
     *   - glyph height
     *   - glyph ascent
     *   - glyph median
     * 2. BODY
     *  following is the glyph line. each glyph takes up a fixed-size cell in which
     *  it is placed at the top left (x = y = 0). the ascender height is at y = 0, the baseline
     *  at y = ascent (from header). the image is a simple byte array with the value of each
     *  pixel taking up exactly three bytes (r-g-b in order). it is saved line-by-line.
     *
     * if the glyph width is larger than the cell width, the character is clipped. the renderer
     * should assume the missing space to be transparent.
     */

    private byte cellWidth;
    private byte cellHeight;
    private char startInclusive;
    private char endInclusive;
    private byte[] data;

    private byte[] meta;

    public int getCharCount() {
        return endInclusive - startInclusive + 1;
    }

    private byte getMeta(char c, int headerIndex) {
        int i = (c - startInclusive) * GLYPH_HEADER_LENGTH + headerIndex;

        byte[] localData = data;
        if (localData != null) {
            return localData[i];
        } else {
            return meta[i];
        }
    }

    int getWidth(char c) {
        return getMeta(c, 0);
    }

    int getHeight(char c) {
        return getMeta(c, 1);
    }

    int getAscent(char c) {
        return getMeta(c, 2);
    }

    /**
     * Free glyph pixels. Glyph pixels will be null after this call but metadata will stay available.
     */
    public void freeData() {
        byte[] localData = data;
        if (localData != null) {
            meta = Arrays.copyOf(localData, GLYPH_HEADER_LENGTH * getCharCount());
            data = null;
        }
    }
}
