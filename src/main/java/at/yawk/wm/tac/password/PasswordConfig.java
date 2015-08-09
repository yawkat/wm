package at.yawk.wm.tac.password;

import at.yawk.wm.style.FontDescriptor;
import java.awt.*;
import java.nio.file.Path;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class PasswordConfig {
    private Path cacheDir;
    /**
     * Password storage timeout in seconds.
     */
    private int timeout;
    private String host;
    private int port = 80;
    private Color editorBackground;
    private FontDescriptor editorFont;
    private int editorWidth;
    private int editorHeight;
}
