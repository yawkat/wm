package at.yawk.wm.x;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    static class Diff {
        private final int mask;
        private final ByteBuffer values;

        @java.beans.ConstructorProperties({ "mask", "values" })
        public Diff(int mask, ByteBuffer values) {
            this.mask = mask;
            this.values = values;
        }

        public boolean isEmpty() {
            return mask == 0;
        }

        public int getMask() {
            return this.mask;
        }

        public ByteBuffer getValues() {
            return this.values;
        }

        public boolean equals(Object o) {
            if (o == this) { return true; }
            if (!(o instanceof Diff)) { return false; }
            final Diff other = (Diff) o;
            if (this.mask != other.mask) { return false; }
            final Object this$values = this.values;
            final Object other$values = other.values;
            if (this$values == null ? other$values != null : !this$values.equals(other$values)) { return false; }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.mask;
            final Object $values = this.values;
            result = result * PRIME + ($values == null ? 0 : $values.hashCode());
            return result;
        }

        public String toString() {
            return "at.yawk.wm.x.MaskAttributeSet.Diff(mask=" + this.mask + ", values=" + this.values + ")";
        }
    }
}
