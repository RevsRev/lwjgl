package github.com.rev.gl.texture;

import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public class Texture implements TextureOperations {
    private final int layer;
    private int texId;

    private boolean initialized;

    public Texture(final int layer) {
        this.layer = layer;
    }

    @Override
    public void init(int width, int height) {
        texId = GL43.glGenTextures();
        resize(width, height);
        initialized = true;
    }

    @Override
    public void bindForReading() {
        if (!initialized) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

        GL43.glActiveTexture(GL43.GL_TEXTURE0 + layer);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
    }

    @Override
    public int[] bindForWriting() {
        if (!initialized) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
        GL43.glFramebufferTexture2D(
                GL43.GL_FRAMEBUFFER,
                layer,
                GL43.GL_TEXTURE_2D,
                texId,
                0
        );
        return new int[]{layer};
    }

    @Override
    public void resize(int width, int height) {
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGBA16F, width, height, 0,
                GL43.GL_RGBA, GL43.GL_FLOAT, (ByteBuffer) null);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_NEAREST);
    }
}
