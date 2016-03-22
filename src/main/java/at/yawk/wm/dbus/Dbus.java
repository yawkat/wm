package at.yawk.wm.dbus;

import at.yawk.dbus.client.DbusClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Singleton
@Slf4j
public class Dbus extends AbstractModule {
    private final DbusClient client = new DbusClient();

    @Override
    protected void configure() {
    }

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

    public void connect() {
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

