package github.com.rev.gl;

import github.com.rev.DynamicV2;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public final class GBuffer {
    private final int[] layers;
    private final int[] texIds;

    public GBuffer(int[] layers) {
        this.layers = layers;
        this.texIds = new int[layers.length];
    }

    public void init(int width, int height) {
        for (int i = 0; i < layers.length; i++) {
            int id = GL43.glGenTextures();
            texIds[i] = id;
            resize(id, width, height);
        }
    }

    public void bindForReading() {
        for (int i = 0; i < texIds.length; i++) {
            GL43.glActiveTexture(GL43.GL_TEXTURE0 + i);
            GL43.glBindTexture(GL43.GL_TEXTURE_2D, texIds[i]);
        }
    }

    public void bindForReading(int layer) {
        GL43.glActiveTexture(GL43.GL_TEXTURE0 + layer);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texIds[layer]);
    }

    public void bindForWriting() {
        for (int i = 0; i < layers.length; i++) {
            GL43.glBindTexture(GL43.GL_TEXTURE_2D, texIds[i]);
            GL43.glFramebufferTexture2D(
                    GL43.GL_FRAMEBUFFER,
                    layers[i],
                    GL43.GL_TEXTURE_2D,
                    texIds[i],
                    0
            );
        }
        GL43.glDrawBuffers(layers);
    }

    public void resize(int width, int height) {
        for (int i = 0; i < layers.length; i++) {
            resize(texIds[i], width, height);
        }
    }

    private void resize(int texId, int width, int height) {
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGBA16F, width, height, 0,
                GL43.GL_RGBA, GL43.GL_FLOAT, (ByteBuffer) null);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_NEAREST);
    }
}
