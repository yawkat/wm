package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dbus.MediaPlayer;
import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.IconWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.Periodic;
import at.yawk.wm.dock.module.RenderElf;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.icon.IconDescriptor;
import at.yawk.wm.x.icon.IconManager;
import at.yawk.yarn.Component;
import java.util.concurrent.Executor;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = 210)
public class MediaWidget extends FlowCompositeWidget {
    @Inject MediaPlayer mediaPlayer;
    @Inject IconManager iconManager;
    @Inject DockConfig config;
    @Inject RenderElf renderElf;

    private IconWidget playing;

    @Inject
    void init(Executor executor, FontManager fontManager) {
        playing = new IconWidget();
        playing.setColor(fontManager.resolve(config.getMediaFont()));
        playing.after(getAnchor(), Direction.HORIZONTAL);
        addWidget(playing);

        mediaPlayer.onPropertiesChanged(() -> executor.execute(() -> {
            update();
            renderElf.render();
        }));
    }

    @Inject
    void bindKeys(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-Pause", mediaPlayer::playPause);
        herbstClient.addKeyHandler("Mod4-Insert", mediaPlayer::previous);
        herbstClient.addKeyHandler("Mod4-Delete", mediaPlayer::next);

        herbstClient.addKeyHandler("XF86AudioPlay", mediaPlayer::playPause);
        herbstClient.addKeyHandler("XF86AudioStop", mediaPlayer::stop);
        herbstClient.addKeyHandler("XF86AudioPrevious", mediaPlayer::previous);
        herbstClient.addKeyHandler("XF86AudioNext", mediaPlayer::next);
    }

    @Periodic(value = 30, render = true)
    void update() {
        IconDescriptor icon;
        if (mediaPlayer.getPlaybackStatus().equals("Playing")) {
            icon = config.getMediaPlaying();
        } else {
            icon = config.getMediaPaused();
        }
        playing.setIcon(iconManager.getIconOrNull(icon));
    }
}
