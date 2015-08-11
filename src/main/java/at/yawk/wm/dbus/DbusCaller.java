package at.yawk.wm.dbus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class DbusCaller {
    void startListener(Bus bus, DEventBus target) {
        Thread thread = new Thread(() -> {
            try {
                listen(bus, target);
            } catch (IOException e) {
                log.error("Failed to listen to dbus {}", bus, e);
            }
        });
        thread.setName("DBus listener " + bus);
        thread.setDaemon(true);
        thread.start();
    }

    void listen(Bus bus, DEventBus target) throws IOException {
        List<String> args = new ArrayList<>();
        args.add("dbus-monitor");
        args.add("--profile");
        Collections.addAll(args, bus.flags);
        Process process = new ProcessBuilder(args)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 7) { continue; }
                DEventBus.EndPoint ep = new DEventBus.EndPoint(bus, parts[4], parts[5], parts[6]);
                log.trace("Received {}", ep);
                target.postUpdate(ep);
            }
        }

        copyErrToLog(process);
    }

    String call(List<String> args) {
        if (log.isTraceEnabled()) { log.trace("Calling {}", String.join(" ", args)); }
        try {
            Process process = new ProcessBuilder(args)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start();

            int status = process.waitFor();
            copyErrToLog(process);
            if (status != 0) { throw new RuntimeException("Status " + status); }

            String reply = getOutput(process);
            if (reply.length() == 0) { return null; }
            if (!reply.substring(0, 17).equals("   variant       ")) {
                throw new UnsupportedOperationException("Unsupported response '" + reply + "'");
            }
            return reply.substring(17);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getOutput(Process process) throws IOException {
        StringBuilder reply = new StringBuilder();
        try (Reader reader = new InputStreamReader(process.getInputStream())) {
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) != -1) {
                reply.append(buf, 0, len);
            }
        }
        return reply.toString();
    }

    private void copyErrToLog(Process process) throws IOException {
        try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) {
                log.warn("[DBus] {}", line);
            }
        }
    }
}
