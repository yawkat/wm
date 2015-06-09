package at.yawk.wm.tac.password;

import at.yawk.password.LocalStorageProvider;
import at.yawk.password.client.ClientValue;
import at.yawk.password.client.PasswordClient;
import at.yawk.password.model.PasswordBlob;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Holds password info until a timeout occurs (auto password lock). Holders have two states: <i>Claimed</i> and
 * <i>unclaimed</i>. A claimed holder always has password info saved while an unclaimed holder may expire.
 *
 * @author yawkat
 */
@RequiredArgsConstructor
class PasswordHolder {
    private final LocalStorageProvider storageProvider;
    private final ScheduledExecutorService executor;
    private final ObjectMapper objectMapper;
    private final InetSocketAddress remote;
    /**
     * Password storage timeout in seconds.
     */
    private final int timeout;

    private Future<?> unclaimFuture = null;

    private boolean claimed = false;
    private Holder holder = null;

    /**
     * Try to claim this holder without supplying a password.
     *
     * @return <code>true</code> if this holder was claimed successfully or was already claimed, <code>false</code> if
     * the holder has no data cached and cannot be claimed without password.
     */
    public synchronized boolean claim() {
        clearUnclaim();
        if (holder == null) {
            return false;
        } else {
            claimed = true;
            return true;
        }
    }

    /**
     * Unclaim this holder and mark it for future clearing.
     */
    public synchronized void unclaim() {
        if (claimed) {
            clearUnclaim();
            claimed = false;
            unclaimFuture = executor.schedule(() -> {
                synchronized (this) {
                    if (!claimed) {
                        clear();
                    }
                }
            }, 10, TimeUnit.MINUTES);
        }
    }

    /**
     * Claim this holder with a password. This is a blocking operation.
     */
    public void claim(String password) throws Exception {
        if (claim()) { return; }

        PasswordClient client = PasswordClient.create();
        client.setPassword(password.getBytes(StandardCharsets.UTF_8));
        client.setObjectMapper(objectMapper);
        client.setRemote(remote);
        client.setLocalStorageProvider(storageProvider);
        ClientValue<PasswordBlob> value = client.load();
        synchronized (this) {
            if (holder == null) {
                holder = new Holder(
                        client,
                        value.getValue() == null ? new PasswordBlob() : value.getValue(),
                        value.isFromLocalStorage()
                );
            }
            claim();
        }
    }

    /**
     * Immediately unclaim and close this holder.
     */
    public synchronized void clear() {
        claimed = false;
        holder = null;
        clearUnclaim();
        // suggest a GC to clear as much sensitive info as possible
        System.gc();
    }

    private void clearUnclaim() {
        if (unclaimFuture != null) {
            unclaimFuture.cancel(false);
            unclaimFuture = null;
        }
    }

    public synchronized PasswordBlob getPasswords() {
        if (!claimed) {
            throw new IllegalStateException();
        }
        return holder.blob;
    }

    public synchronized void save() throws Exception {
        if (!claimed) {
            throw new IllegalStateException();
        }
        holder.client.save(holder.blob);
    }

    @Value
    private static class Holder {
        private final PasswordClient client;
        private final PasswordBlob blob;
        private final boolean fromLocalStorage;
    }
}
