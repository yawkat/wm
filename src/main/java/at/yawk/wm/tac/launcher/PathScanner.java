package at.yawk.wm.tac.launcher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
class PathScanner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PathScanner.class);
    private final List<Path> path;
    private List<String> applications;

    public PathScanner(List<Path> path) {
        this.path = path;
    }

    public PathScanner() {
        this(getEnvironmentPath());
    }

    public List<String> getApplications() {
        return applications == null ? Collections.emptyList() : applications;
    }

    public boolean isScanned() {
        return applications != null;
    }

    public void scan() throws UncheckedIOException {
        log.info("Scanning PATH for applications...");
        List<String> apps = path.stream()
                .flatMap(dir -> {
                    try {
                        return Files.list(dir);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .map(s -> s.getFileName().toString())
                .unordered().distinct()
                .sorted()
                .collect(Collectors.toList());
        log.info("Found {} applications.", apps.size());
        applications = apps;
    }

    private static List<Path> getEnvironmentPath() {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Paths::get)
                .collect(Collectors.toList());
    }
}
