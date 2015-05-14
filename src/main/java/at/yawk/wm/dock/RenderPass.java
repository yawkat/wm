package at.yawk.wm.dock;

import at.yawk.wm.x.Graphics;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class RenderPass {
    final Graphics graphics;
    final boolean exposePass;
}
