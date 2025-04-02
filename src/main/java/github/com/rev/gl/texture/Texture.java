package github.com.rev.gl.texture;

public abstract class Texture {
    private final int layer;

    protected Texture(int layer) {
        this.layer = layer;
    }
}
