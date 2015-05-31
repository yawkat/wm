package at.yawk.wm.dock;

import java.util.*;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author yawkat
 */
@NotThreadSafe
final class LayoutManager implements WidgetSet {
    private final Set<Widget> widgets = new HashSet<>();
    private Widget[] bakedWidgets;
    private boolean dirty = true;

    @Override
    public void addWidget(Widget widget) {
        widgets.add(widget);
        dirty = true;
    }

    private void bakeWidgets() {
        if (bakedWidgets == null || bakedWidgets.length != widgets.size()) {
            bakedWidgets = new Widget[widgets.size()];
        }

        int i = 0;
        Set<Widget> visited = new HashSet<>();
        Deque<Widget> visitQueue = new ArrayDeque<>(widgets);
        while (!visitQueue.isEmpty()) {
            Widget toVisit = visitQueue.peekFirst();
            if (visited.contains(toVisit)) {
                visitQueue.pollFirst();
                continue;
            }
            boolean missingDependencies = false;
            for (Widget dependency : toVisit.layoutDependencies) {
                if (!visited.contains(dependency)) {
                    visitQueue.addFirst(dependency);
                    missingDependencies = true;
                }
            }
            if (!missingDependencies) {
                bakedWidgets[i++] = toVisit;
                visited.add(toVisit);
            }
        }

        Arrays.sort(bakedWidgets, Comparator.comparingInt(widget -> widget.getZ()));
    }

    public void render(RenderPass pass) {
        if (dirty) {
            bakeWidgets();
            dirty = false;
        }
        for (Widget widget : bakedWidgets) {
            widget.preRender(pass);
        }
        for (Widget widget : bakedWidgets) {
            widget.internalRender(pass);
        }
    }
}
