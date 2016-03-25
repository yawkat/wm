package at.yawk.wm.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * "Collection" that exists to keep references to stuff.
 *
 * @author yawkat
 */
class CollectorSink {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Object> references = new ArrayList<>();

    public void add(Object o) {
        references.add(o);
    }
}
