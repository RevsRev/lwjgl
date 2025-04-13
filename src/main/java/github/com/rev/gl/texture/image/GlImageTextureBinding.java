package github.com.rev.gl.texture.image;

import github.com.rev.gl.shader.ShaderReadable;
import lombok.Getter;
import org.lwjgl.opengl.GL43;

public final class GlImageTextureBinding implements ShaderReadable {

    private final GlImageTexture glImageTexture;
    @Getter
    private final int layer;

    private GlImageTextureBinding(GlImageTexture glImageTexture, final int layer) {
        this.glImageTexture = glImageTexture;
        this.layer = layer;
    }

    public static GlImageTextureBinding fromFile(final String filePath, final int layer) {
        GlImageTexture imageTexture = GlImageTexture.fromFile(filePath);
        return new GlImageTextureBinding(imageTexture, layer);
    }

    public void init() {
        glImageTexture.init();
    }

    @Override
    public void bindForReading(int location) {
        if (!glImageTexture.isInitialized()) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

        GL43.glUniform1i(location, layer);
        GL43.glActiveTexture(GL43.GL_TEXTURE0 + layer);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, glImageTexture.getTexId());
    }
}
