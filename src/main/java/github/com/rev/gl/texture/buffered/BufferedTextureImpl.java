package github.com.rev.gl.texture.buffered;

import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public final class BufferedTextureImpl implements BufferedTexture {
    private final String name;
    private final int readLayer;
    private final int writeLayer;
    private int texId;

    private boolean initialized;

    public BufferedTextureImpl(final String name, final int readLayer, final int writeLayer) {
        this.name = name;
        this.readLayer = readLayer;
        this.writeLayer = writeLayer;
    }

    @Override
    public void init(int width, int height) {
        texId = GL43.glGenTextures();
        resize(width, height);
        initialized = true;
    }

    @Override
    public void bindForReading(int shaderProgram) {
        if (!initialized) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

        GL43.glUniform1i(GL43.glGetUniformLocation(shaderProgram, name), readLayer);

        GL43.glActiveTexture(GL43.GL_TEXTURE0 + readLayer);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
    }

    @Override
    public int[] bindForWriting() {
        if (!initialized) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

//        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
        GL43.glFramebufferTexture2D(
                GL43.GL_FRAMEBUFFER,
                GL43.GL_COLOR_ATTACHMENT0 + writeLayer,
                GL43.GL_TEXTURE_2D,
                texId,
                0
        );
        return new int[]{GL43.GL_COLOR_ATTACHMENT0 + writeLayer};
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
