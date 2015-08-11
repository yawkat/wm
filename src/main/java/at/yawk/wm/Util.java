package at.yawk.wm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * @author yawkat
 */
public class Util {
    private Util() {}

    /**
     * @param tryMaxCount The maximum output length
     */
    public static List<String> split(String input, char delimiter, int tryMaxCount) {
        List<String> found = new ArrayList<>(tryMaxCount > 4 ? 4 : tryMaxCount);
        StringBuilder currentEntry = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == delimiter) {
                if (currentEntry.length() != 0) {
                    found.add(currentEntry.toString());
                    currentEntry.setLength(0);
                    if (found.size() >= tryMaxCount) {
                        break;
                    }
                }
            } else {
                currentEntry.append(c);
            }
        }
        if (found.size() < tryMaxCount && currentEntry.length() > 0) {
            found.add(currentEntry.toString());
        }
        return found;
    }

    /**
     * Taken from guava, this returns 0 for a/A, 25 for z/Z and a larger value for any non-letter.
     */
    public static int alphabetIndex(char c) {
        return (char) ((c | 0x20) - 'a');
    }

    public static boolean startsWithIgnoreCaseAscii(String s, String prefix) {
        if (prefix.length() > s.length()) { return false; }
        for (int i = 0; i < prefix.length(); i++) {
            char o = s.charAt(i);
            char p = prefix.charAt(i);
            if (o != p) {
                int ao = alphabetIndex(o);
                if (ao > 26 || ao != alphabetIndex(p)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean containsIgnoreCaseAscii(String haystack, String needle) {
        if (needle.length() > haystack.length()) { return false; }

        outer:
        for (int i = 0; i <= haystack.length() - needle.length(); i++) {
            for (int j = 0; j < needle.length(); j++) {
                char ch = haystack.charAt(i + j);
                char cn = needle.charAt(j);
                if (ch != cn) {
                    int ih = alphabetIndex(ch);
                    int in = alphabetIndex(cn);
                    if (ih != in || ih >= 26) {
                        // no match
                        continue outer;
                    }
                }
            }
            // match, return
            return true;
        }
        return false;
    }

    public static BufferedImage loadImage(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return ImageIO.read(in);
        }
    }
}
