package at.yawk.wm.dbus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class DbusCaller {
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
