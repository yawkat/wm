package at.yawk.wm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yawkat
 */
public class Util {
    private Util() {}

    public static List<String> split(String input, char delimiter, int tryMaxCount) {
        List<String> found = new ArrayList<>(tryMaxCount > 4 ? 4 : tryMaxCount);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == delimiter) {
                if (builder.length() != 0) {
                    found.add(builder.toString());
                    builder.setLength(0);
                    if (found.size() >= tryMaxCount) {
                        break;
                    }
                }
            } else {
                builder.append(c);
            }
        }
        if (found.size() < tryMaxCount && builder.length() > 0) {
            found.add(builder.toString());
        }
        return found;
    }
}
