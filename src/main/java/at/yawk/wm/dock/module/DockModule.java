package at.yawk.wm.dock.module;

import at.yawk.wm.Main;
import dagger.Module;

/**
 * @author yawkat
 */
@Module(
        includes = { Main.class },
        injects = { DockBuilder.class }
)
public class DockModule {}
