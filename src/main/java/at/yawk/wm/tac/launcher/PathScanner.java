package at.yawk.wm.tac.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yawkat
 */
class PathScanner {
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

    public void scan() throws IOException {
        List<String> apps = new ArrayList<>();
        for (Path p : path) {
            apps.addAll(
                    Files.list(p).map(s -> s.getFileName().toString())
                            .collect(Collectors.toList())
            );
        }
        Collections.sort(apps);
        applications = apps;
    }

    private static List<Path> getEnvironmentPath() {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Paths::get)
                .collect(Collectors.toList());
    }
}
