package at.yawk.wm.tac.launcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author yawkat
 */
class REPL {
    private static final char[] CLOSE_CHARS = new char[256];

    static {
        CLOSE_CHARS['('] = ')';
        CLOSE_CHARS['"'] = '"';
        CLOSE_CHARS['\''] = '\'';
        CLOSE_CHARS['['] = ']';
        CLOSE_CHARS['{'] = '}';
    }

    public String run(String command) {
        Result firstResult = null;
        Result result;
        while (true) {
            try {
                result = doWrite(command);
            } catch (IOException | InterruptedException e) {
                return e.getMessage();
            }
            if (result.code == 0) {
                // good command, use this
                return result.message;
            }
            if (firstResult == null) {
                firstResult = result;
            }
            command = fix(command);
            if (command == null) {
                // unfixable, use first error
                return firstResult.message;
            }
        }
    }

    private static String fix(String command) {
        int expectedEndingsLen = 0;
        char[] expectedEndings = new char[4];

        outer:
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            for (int j = expectedEndingsLen - 1; j >= 0; j--) {
                if (expectedEndings[j] == c) {
                    expectedEndingsLen = j;
                    continue outer;
                }
            }

            if (c < CLOSE_CHARS.length && CLOSE_CHARS[c] != 0) {
                if (expectedEndingsLen == expectedEndings.length) {
                    expectedEndings = Arrays.copyOf(expectedEndings, expectedEndings.length * 2);
                }
                expectedEndings[expectedEndingsLen++] = CLOSE_CHARS[c];
            }
        }

        if (expectedEndingsLen == 0) {
            return null; // can't fix anything
        } else {
            return command + expectedEndings[expectedEndingsLen - 1];
        }
    }

    private Result doWrite(String command) throws IOException, InterruptedException {
        String code = "print(" + command + ")";
        ProcessBuilder builder = new ProcessBuilder("python", "-c", code);
        Process process = builder.start();
        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return new Result(-1, "Timeout");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copy(process.getInputStream(), buf);
        copy(process.getErrorStream(), buf);
        return new Result(process.waitFor(), buf.toString("UTF-8"));
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] bytes = new byte[256];
        int len;
        while ((len = from.read(bytes)) != -1) {
            to.write(bytes, 0, len);
        }
    }

    private static class Result {
        int code;
        String message;

        @java.beans.ConstructorProperties({ "code", "message" })
        public Result(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }

        public boolean equals(Object o) {
            if (o == this) { return true; }
            if (!(o instanceof Result)) { return false; }
            final Result other = (Result) o;
            if (this.code != other.code) { return false; }
            final Object this$message = this.message;
            final Object other$message = other.message;
            if (this$message == null ? other$message != null : !this$message.equals(other$message)) { return false; }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.code;
            final Object $message = this.message;
            result = result * PRIME + ($message == null ? 0 : $message.hashCode());
            return result;
        }

        public String toString() {
            return "at.yawk.wm.tac.launcher.REPL.Result(code=" + this.code + ", message=" + this.message + ")";
        }
    }
}
