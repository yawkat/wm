package at.yawk.wm.x.icon;

import org.testng.annotations.Test;

import static at.yawk.wm.x.icon.ColorizingPixelTransformer.blendColor;
import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class ColorizingPixelTransformerTest {
    @Test
    public void testBlendColor() throws Exception {
        // it doesn't really matter if these are off by one or two
        assertEquals(blendColor(0xff, 0, (byte) 0xff), (byte) 0xff);
        assertEquals(blendColor(0xff, 0, (byte) 0x10), (byte) 0x10);
        assertEquals(blendColor(16, 0, (byte) 128), (byte) 8);
    }
}