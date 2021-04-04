package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dbus.MediaPlayer;
import at.yawk.wm.ui.Direction;
import at.yawk.wm.ui.FlowCompositeWidget;
import at.yawk.wm.ui.IconWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.hl.HerbstClient;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT, priority = 210)
public class MediaWidget extends FlowCompositeWidget {
    @Inject MediaPlayer mediaPlayer;
    @Inject HerbstClient herbstClient;

    private IconWidget playing; // todo

    @Override
    public void init() {
        playing = new IconWidget();
        playing.setColor(DockConfig.INSTANCE.getMediaFont());
        playing.after(getAnchor(), Direction.HORIZONTAL);
        addWidget(playing);

        herbstClient.addKeyHandler("Mod4-Pause", mediaPlayer::playPause);
        herbstClient.addKeyHandler("Mod4-Insert", mediaPlayer::previous);
        herbstClient.addKeyHandler("Mod4-Delete", mediaPlayer::next);

        herbstClient.addKeyHandler("XF86AudioPlay", mediaPlayer::playPause);
        herbstClient.addKeyHandler("XF86AudioStop", mediaPlayer::stop);
        herbstClient.addKeyHandler("XF86AudioPrevious", mediaPlayer::previous);
        herbstClient.addKeyHandler("XF86AudioNext", mediaPlayer::next);
    }
}
