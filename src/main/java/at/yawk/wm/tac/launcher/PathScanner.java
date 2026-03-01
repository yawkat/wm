package at.yawk.wm.tac.launcher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
class PathScanner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PathScanner.class);
    private final List<Path> path;
    private List<Path> applications;

    public PathScanner(List<Path> path) {
        this.path = path;
    }

    public PathScanner() {
        this(getEnvironmentPath());
    }

    public List<Path> getApplications() {
        return applications == null ? Collections.emptyList() : applications;
    }

    public boolean isScanned() {
        return applications != null;
    }

    public void scan() throws UncheckedIOException {
        log.info("Scanning PATH for applications...");
        SortedSet<Path> apps = path.stream()
                .flatMap(dir -> {
                    try {
                        return Files.list(dir);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .unordered()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Path::getFileName))));
        log.info("Found {} applications.", apps.size());
        applications = new ArrayList<>(apps);
    }

    private static List<Path> getEnvironmentPath() {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Paths::get)
                .collect(Collectors.toList());
    }
}
