package at.yawk.wm.x;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.Value;

/**
 * @author yawkat
 */
class MaskAttributeSet {
    private static final int FIELD_COUNT = 32;

    private final int[] state = new int[FIELD_COUNT];
    private final int[] flushed = new int[FIELD_COUNT];

    public void set(int key, int value) {
        state[Integer.numberOfTrailingZeros(key)] = value;
    }

    public Diff flush() {
        int mask = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(FIELD_COUNT).order(ByteOrder.nativeOrder());
        for (int i = 0; i < FIELD_COUNT; i++) {
            if (flushed[i] != state[i]) {
                buffer.putInt(flushed[i] = state[i]);
                mask |= 1 << i;
            }
        }
        return new Diff(mask, buffer);
    }

    @Value
    static class Diff {
        private final int mask;
        private final ByteBuffer values;

        public boolean isEmpty() {
            return mask == 0;
        }
    }
}
