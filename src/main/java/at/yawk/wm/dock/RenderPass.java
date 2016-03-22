package at.yawk.wm.dock;

import at.yawk.wm.x.Graphics;

/**
 * @author yawkat
 */
class RenderPass {
    final Graphics graphics;
    final boolean exposePass;

    @java.beans.ConstructorProperties({ "graphics", "exposePass" })
    public RenderPass(Graphics graphics, boolean exposePass) {
        this.graphics = graphics;
        this.exposePass = exposePass;
    }
}
