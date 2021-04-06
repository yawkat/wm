package at.yawk.wm.dbus;

import at.yawk.dbus.client.DbusClient;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
public class Dbus {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Dbus.class);
    private final DbusClient client = new DbusClient();

    public MediaPlayer mediaPlayer() {
        return implement(MediaPlayer.class);
    }

    public NetworkManager networkManager() {
        return implement(NetworkManager.class);
    }

    public Power power() {
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

