package at.yawk.wm.style;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Singleton
public class FontManager {
    private Map<FontDescriptor, FontStyle> fonts;

    @Inject
    void loadConfig(StyleConfig config) {
        fonts = config.getFonts();

        Set<FontDescriptor> visited = new HashSet<>();
        for (FontDescriptor descriptor : fonts.keySet()) {
            bakeStyle(descriptor, visited);
            visited.clear();
        }
    }

    public FontStyle resolve(FontDescriptor descriptor) {
        if (descriptor instanceof AnonymousFontDescriptor) {
            return ((AnonymousFontDescriptor) descriptor).getStyle();
        } else {
            FontStyle style = fonts.get(descriptor);
            if (style == null) { throw new NoSuchElementException(descriptor.toString()); }
            return style;
        }
    }

    public FontDescriptor compute(@Nonnull FontTransition transition, float progress) {
        return new AnonymousFontDescriptor(transition.computeStyle(this, progress));
    }

    private FontStyle bakeStyle(FontDescriptor descriptor, Set<FontDescriptor> visited) {
        if (!visited.add(descriptor)) {
            throw new RuntimeException("Circular font inheritance involving: " + visited);
        }
        FontStyle style = fonts.get(descriptor);
        if (style == null) { throw new NoSuchElementException(descriptor.toString()); }
        if (style.getParent() != null) {
            FontStyle parent = bakeStyle(style.getParent(), visited);

            if (style.getForeground() == null) { style.setForeground(parent.getForeground()); }
            if (style.getBackground() == null) { style.setBackground(parent.getBackground()); }
            if (style.getBold() == null) { style.setBold(parent.getBold()); }
            if (style.getItalic() == null) { style.setItalic(parent.getItalic()); }
            if (style.getFamily() == null) { style.setFamily(parent.getFamily()); }
            if (style.getSize() == null) { style.setSize(parent.getSize()); }
            style.setParent(null);
        }
        style.validate(descriptor);
        visited.remove(descriptor);
        return style;
    }
}
