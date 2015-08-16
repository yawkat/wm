package at.yawk.wm.dbus;

import at.yawk.dbus.client.DbusClient;
import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class Dbus {
    private final DbusClient client = new DbusClient();

    @Provides
    MediaPlayer mediaPlayer() {
        return implement(MediaPlayer.class);
    }

    @Provides
    NetworkManager networkManager() {
        return implement(NetworkManager.class);
    }

    @Provides
    Power power() {
        return implement(Power.class);
    }

    @PostConstruct
    void startListeners() {
        try {
            client.connectSystem();
            client.connectSession();
        } catch (Exception e) {
            log.error("Failed to connect to dbus", e);
        }
    }

    private <I> I implement(Class<I> itf) {
        return client.implement(itf);
    }
}

