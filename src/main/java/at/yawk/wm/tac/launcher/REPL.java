package at.yawk.wm.tac.launcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import lombok.Value;

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
        String code = "print(eval('" + command.replace("\\", "\\\\").replace("'", "\\") + "'))";
        ProcessBuilder builder = new ProcessBuilder("python", "-c", code);
        Process process = builder.start();
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

    @Value
    private static class Result {
        int code;
        String message;
    }
}
