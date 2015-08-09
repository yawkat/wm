package at.yawk.wm.x.font;

import at.yawk.wm.style.FontStyle;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

/**
 * @author yawkat
 */
class GlyphFileFactory {
    private static final boolean DEBUG_LINES = false;

    private final byte cellWidth;
    private final byte cellHeight;
    private final FontStyle style;
    private final Font font;

    public GlyphFileFactory(byte cellWidth, byte cellHeight, FontStyle style) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.style = style;
        this.font = style.getFamily().createFont(style);
    }

    public GlyphFile renderRange(char startInclusive, char endInclusive) {
        GlyphFile glyphFile = new GlyphFile();
        glyphFile.setStartInclusive(startInclusive);
        glyphFile.setEndInclusive(endInclusive);
        glyphFile.setCellWidth(cellWidth);
        glyphFile.setCellHeight(cellHeight);

        int nChars = glyphFile.getCharCount();
        int headerLen = nChars * GlyphFile.GLYPH_HEADER_LENGTH;
        byte[] data = new byte[
                headerLen + // header
                nChars * cellHeight * cellWidth * GlyphFile.GLYPH_HEADER_LENGTH // glyphs
                ];

        int rowWidth = cellWidth * nChars;
        BufferedImage image = new BufferedImage(
                rowWidth, cellHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D gfx = image.createGraphics();
        gfx.setFont(font);
        gfx.setBackground(style.getBackground());
        gfx.clearRect(0, 0, rowWidth, cellHeight);
        gfx.setColor(style.getForeground());
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // todo config option
        FontMetrics metrics = gfx.getFontMetrics();
        for (char c = startInclusive; c <= endInclusive; c++) {
            String s = Character.toString(c);
            LineMetrics lineMetrics = metrics.getLineMetrics(s, gfx);
            int x = (c - startInclusive) * cellWidth;
            gfx.drawString(s, x, lineMetrics.getAscent());

            int headerPos = (c - startInclusive) * GlyphFile.GLYPH_HEADER_LENGTH;
            int width = metrics.charWidth(c);
            if (width > cellWidth) {
                gfx.clearRect(x + cellWidth, 0, width - cellWidth, cellHeight);
            }

            data[headerPos] = (byte) width;
            data[headerPos + 1] = (byte) lineMetrics.getHeight();
            data[headerPos + 2] = (byte) lineMetrics.getAscent();

            if (DEBUG_LINES) {
                gfx.setColor(Color.WHITE);
                gfx.drawLine(x + cellWidth - 1, 0, x + cellWidth - 1, cellHeight);
                gfx.setColor(Color.RED);
                gfx.drawLine(x + width, 0, x + width, cellHeight);
                int lh = (int) lineMetrics.getHeight();
                gfx.drawLine(x, lh, x + width, lh);
                gfx.setColor(Color.YELLOW);
                gfx.drawLine(x, (int) (lh - lineMetrics.getAscent()), x + width, (int) (lh - lineMetrics.getAscent()));

                // reset color
                gfx.setColor(style.getForeground());
            }
        }
        gfx.dispose();

        int[] pixels = image.getRGB(0, 0, nChars * cellWidth, cellHeight, null, 0, nChars * cellWidth);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int pos = headerLen + i * 3;
            data[pos] = (byte) (pixel >> 16); // r
            data[pos + 1] = (byte) (pixel >> 8); // g
            data[pos + 2] = (byte) pixel; // b
        }

        glyphFile.setData(data);

        return glyphFile;
    }

}
