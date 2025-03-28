package github.com.rev.gl.texture;

import lombok.Getter;

public class SwappingTexture implements TextureOperations {

    private final Textures first;
    private final Textures second;

    @Getter
    private boolean swap = false;

    public SwappingTexture(Textures first, Textures second) {
        this.first = first;
        this.second = second;
    }

    public void swap() {
        swap = !swap;
    }

    @Override
    public void init(int width, int height) {
        first.init(width, height);
        second.init(width, height);
    }

    @Override
    public void bindForReading() {
        if (swap) {
            first.bindForReading();
        } else {
            second.bindForReading();
        }
    }

    @Override
    public int[] bindForWriting() {
        if (swap) {
            return second.bindForWriting();
        } else {
            return first.bindForWriting();
        }
    }

    @Override
    public void resize(int width, int height) {
        first.resize(width, height);
        second.resize(width, height);
    }
}
