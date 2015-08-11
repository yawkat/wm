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
    void init(FontManager fontManager) {
        playing = new IconWidget();
        playing.setColor(fontManager.resolve(config.getMediaFont()));
        playing.after(getAnchor(), Direction.HORIZONTAL);
        addWidget(playing);
    }

    @Inject
    void bindKeys(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-Pause", this::playPause);
        herbstClient.addKeyHandler("Mod4-Insert", this::previous);
        herbstClient.addKeyHandler("Mod4-Delete", this::next);

        herbstClient.addKeyHandler("XF86AudioPlay", this::playPause);
        herbstClient.addKeyHandler("XF86AudioStop", this::stop);
        herbstClient.addKeyHandler("XF86AudioPrevious", this::previous);
        herbstClient.addKeyHandler("XF86AudioNext", this::next);
    }

    private void playPause() {
        mediaPlayer.playPause();
        asyncUpdate();
    }

    private void next() {
        mediaPlayer.next();
        asyncUpdate();
    }

    private void previous() {
        mediaPlayer.previous();
        asyncUpdate();
    }

    private void stop() {
        mediaPlayer.stop();
        asyncUpdate();
    }

    private void asyncUpdate() {
        update();
        renderElf.render();
    }

    @Periodic(value = 10, render = true)
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
