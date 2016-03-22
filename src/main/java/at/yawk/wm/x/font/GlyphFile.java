package at.yawk.wm.x.font;

import java.util.Arrays;

/**
 * @author yawkat
 */
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

    public GlyphFile() {
    }

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

    public byte getCellWidth() {
        return this.cellWidth;
    }

    public byte getCellHeight() {
        return this.cellHeight;
    }

    public char getStartInclusive() {
        return this.startInclusive;
    }

    public char getEndInclusive() {
        return this.endInclusive;
    }

    public byte[] getData() {
        return this.data;
    }

    public byte[] getMeta() {
        return this.meta;
    }

    public void setCellWidth(byte cellWidth) {
        this.cellWidth = cellWidth;
    }

    public void setCellHeight(byte cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setStartInclusive(char startInclusive) {
        this.startInclusive = startInclusive;
    }

    public void setEndInclusive(char endInclusive) {
        this.endInclusive = endInclusive;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setMeta(byte[] meta) {
        this.meta = meta;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof GlyphFile)) { return false; }
        final GlyphFile other = (GlyphFile) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (this.cellWidth != other.cellWidth) { return false; }
        if (this.cellHeight != other.cellHeight) { return false; }
        if (this.startInclusive != other.startInclusive) { return false; }
        if (this.endInclusive != other.endInclusive) { return false; }
        if (!Arrays.equals(this.data, other.data)) { return false; }
        if (!Arrays.equals(this.getMeta(), other.getMeta())) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.cellWidth;
        result = result * PRIME + this.cellHeight;
        result = result * PRIME + this.startInclusive;
        result = result * PRIME + this.endInclusive;
        result = result * PRIME + Arrays.hashCode(this.data);
        result = result * PRIME + Arrays.hashCode(this.meta);
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof GlyphFile;
    }

    public String toString() {
        return "at.yawk.wm.x.font.GlyphFile(cellWidth=" + this.cellWidth + ", cellHeight=" + this.cellHeight +
               ", startInclusive=" + this.startInclusive + ", endInclusive=" + this.endInclusive + ", data=" +
               Arrays.toString(this.data) + ", meta=" + Arrays.toString(this.getMeta()) + ")";
    }
}
