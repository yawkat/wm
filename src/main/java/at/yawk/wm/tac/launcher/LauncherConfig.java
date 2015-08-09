package at.yawk.wm.tac.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class LauncherConfig {
    private Map<String, JsonNode> shortcuts;
    private String[] shutdownCommand;
}
