package github.com.rev.gl.texture.image;

import github.com.rev.gl.shader.ShaderReadable;
import lombok.Getter;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

public final class ImageTexture implements ShaderReadable {

    private final int width;
    private final int height;
    @Getter
    private final int layer;
    private final ByteBuffer data;

    private int texId;
    private boolean initialized = false;

    public ImageTexture(int width, int height, int layer, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.layer = layer;
        this.data = data;
    }

    public static ImageTexture fromFile(final String filePath, final int width, final int height, final int layer) {
        ByteBuffer byteBuffer = STBImage.stbi_load(filePath, new int[] {width}, new int[] {height}, new int[] {3}, 0);
        return new ImageTexture(width, height, layer, byteBuffer);
    }

    public void init() {
        texId = GL43.glGenTextures();
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);

//        float[] borderColor = {1.0f, 1.0f, 0.0f, 1.0f};
//        GL43.glTexParameterfv(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_BORDER_COLOR, borderColor);

        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_S, GL43.GL_REPEAT);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_T, GL43.GL_REPEAT);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_LINEAR);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_LINEAR);
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGB, width, height, 0,
                GL43.GL_RGB, GL43.GL_UNSIGNED_BYTE, data);
        GL43.glGenerateMipmap(GL43.GL_TEXTURE_2D);

        initialized = true;
    }

    @Override
    public void bindForReading(int location) {
        if (!initialized) {
            throw new RuntimeException("Trying to use a texture before it has been initialized");
        }

        GL43.glUniform1i(location, layer);

        GL43.glActiveTexture(GL43.GL_TEXTURE0 + layer);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);
    }
}
