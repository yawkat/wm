package at.yawk.wm.style;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
class AnonymousFontDescriptor implements FontDescriptor {
    private final FontStyle style;
}
