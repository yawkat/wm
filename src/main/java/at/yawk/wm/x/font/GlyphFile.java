package at.yawk.wm.x.font;

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

    public int getCharCount() {
        return endInclusive - startInclusive + 1;
    }

    int getWidth(char c) {
        int i = c - startInclusive;
        return data[i * GLYPH_HEADER_LENGTH];
    }

    int getHeight(char c) {
        int i = c - startInclusive;
        return data[i * GLYPH_HEADER_LENGTH + 1];
    }

    int getAscent(char c) {
        int i = c - startInclusive;
        return data[i * GLYPH_HEADER_LENGTH + 2];
    }
}
