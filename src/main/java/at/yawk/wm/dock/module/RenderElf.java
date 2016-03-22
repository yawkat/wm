package at.yawk.wm.dock.module;

import com.google.inject.ImplementedBy;

/**
 * @author yawkat
 */
@ImplementedBy(DockBuilder.class)
public interface RenderElf {
    void render();
}
