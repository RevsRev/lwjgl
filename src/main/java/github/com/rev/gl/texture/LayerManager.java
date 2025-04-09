package github.com.rev.gl.texture;

import java.util.concurrent.atomic.AtomicInteger;

public final class LayerManager {

    private static final LayerManager INSTANCE = new LayerManager();

    private final AtomicInteger layer = new AtomicInteger(0);

    public static synchronized LayerManager instance() {
        return INSTANCE;
    }

    public int next() {
        return layer.getAndIncrement();
    }

}
