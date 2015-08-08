package at.yawk.wm.tac.launcher;

import at.yawk.yarn.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@Component
public class ApplicationRunner {
    public void run(Command command) {
        try {
            log.info("Executing {}", command);

            List<String> line;
            if (command.isJail()) {
                line = new ArrayList<>();
                line.add("firejail");
                line.add("--");
                Collections.addAll(line, command.getCommand());
            } else {
                line = Arrays.asList(command.getCommand());
            }

            log.debug("Final command line: {}", line);

            new ProcessBuilder()
                    .command(line)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start();
        } catch (IOException e) {
            log.warn("Failed to run command {}", command, e);
        }
    }
}
