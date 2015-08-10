package at.yawk.wm.tac.password;

import at.yawk.password.LocalStorageProvider;
import at.yawk.password.client.ClientValue;
import at.yawk.password.client.PasswordClient;
import at.yawk.password.model.PasswordBlob;
import at.yawk.wm.Scheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds password info until a timeout occurs (auto password lock). Holders have two states: <i>Claimed</i> and
 * <i>unclaimed</i>. A claimed holder always has password info saved while an unclaimed holder may expire.
 *
 * @author yawkat
 */
@Slf4j
@RequiredArgsConstructor
class PasswordHolder {
    private final LocalStorageProvider storageProvider;
    private final Scheduler scheduler;
    private final ObjectMapper objectMapper;
    private final InetSocketAddress remote;
    /**
     * Password storage timeout in seconds.
     */
    private final int timeout;

    private Future<?> unclaimFuture = null;

    private int claimCount = 0;
    private Holder holder = null;

    /**
     * Try to claim this holder without supplying a password.
     *
     * @return A {@link at.yawk.wm.tac.password.PasswordHolder.HolderClaim} object if this holder was claimed
     * successfully or was already claimed, <code>null</code> if the holder has no data cached and cannot be
     * claimed without password.
     */
    @Nullable
    public synchronized HolderClaim claim() {
        log.debug("Attempting to claim password holder without password");
        clearUnclaim();
        if (holder == null) {
            return null;
        } else {
            return createClaim();
        }
    }

    /**
     * Unclaim one claim on this holder and clear if needed.
     */
    private synchronized void internalUnclaim() {
        claimCount--;
        log.debug("Discarded claim, now have {} claims", claimCount);
        if (claimCount <= 0) {
            log.debug("Unclaiming password holder, scheduling clear");
            clearUnclaim();
            unclaimFuture = scheduler.schedule(() -> {
                synchronized (PasswordHolder.this) {
                    if (claimCount <= 0) {
                        log.debug("Running scheduled clear");
                        clear();
                    } else {
                        log.debug("Aborting scheduled clear because a new claim was added");
                    }
                }
            }, 10, TimeUnit.MINUTES);
        }
    }

    /**
     * Claim this holder with a password. This is a blocking operation.
     */
    @Nonnull
    public HolderClaim claim(String password) throws Exception {
        log.info("Attempting to claim password holder with password");
        HolderClaim noPasswordClaim = claim();
        if (noPasswordClaim != null) { return noPasswordClaim; }

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
            HolderClaim claim = claim();
            assert claim != null;
            return claim;
        }
    }

    /**
     * Immediately unclaim and close this holder.
     */
    private synchronized void clear() {
        claimCount = 0;
        holder = null;
        clearUnclaim();
        // suggest a GC to clear as much sensitive info as possible
        System.gc();
    }

    private synchronized void clearUnclaim() {
        if (unclaimFuture != null) {
            unclaimFuture.cancel(false);
            unclaimFuture = null;
        }
    }

    public synchronized PasswordBlob getPasswords() {
        if (claimCount <= 0) {
            throw new IllegalStateException();
        }
        return holder.blob;
    }

    public synchronized boolean isFromLocalStorage() {
        if (claimCount <= 0) {
            throw new IllegalStateException();
        }
        return holder.fromLocalStorage;
    }

    public synchronized void save() throws Exception {
        if (claimCount <= 0) {
            throw new IllegalStateException();
        }
        holder.client.save(holder.blob);
    }

    /**
     * Increment the claim counter and return a new claim object.
     */
    @Nonnull
    private synchronized PasswordHolder.HolderClaim createClaim() {
        if (holder == null) { throw new IllegalStateException(); }

        claimCount++;
        log.debug("Created claim, now have {} claims", claimCount);
        AtomicBoolean unclaimed = new AtomicBoolean(false);
        return new HolderClaim() {
            @Override
            public void unclaim() {
                if (unclaimed.compareAndSet(false, true)) {
                    PasswordHolder.this.internalUnclaim();
                }
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();

                if (!unclaimed.get()) {
                    log.warn("Orphaned HolderClaim object");
                }
            }
        };
    }

    @Value
    private static class Holder {
        private final PasswordClient client;
        private final PasswordBlob blob;
        private final boolean fromLocalStorage;
    }

    public interface HolderClaim {
        void unclaim();
    }
}
