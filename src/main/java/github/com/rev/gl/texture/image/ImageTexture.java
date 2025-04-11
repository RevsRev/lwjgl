package github.com.rev.gl.texture.image;

import github.com.rev.gl.shader.ShaderReadable;
import lombok.Getter;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class ImageTexture implements ShaderReadable {

    private final int width;
    private final int height;
    private final int glType;
    @Getter
    private final int layer;
    private final ByteBuffer data;

    private int texId;
    private boolean initialized = false;

    private ImageTexture(int width, int height, int glType, int layer, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.glType = glType;
        this.layer = layer;
        this.data = data;
    }

    public static ImageTexture fromFile(final String filePath, final int layer) {

        final int height;
        final int width;
        final int imageIoType;
        try {
            BufferedImage read = ImageIO.read(new File(filePath));
            height = read.getHeight();
            width = read.getWidth();
            imageIoType = read.getType();
        } catch (IOException e) {
            throw new RuntimeException("Could not read image file", e);
        }

        int glType = getGlType(imageIoType);
        int numChannels = glType == GL43.GL_RGBA ? 3 : 4;
        ByteBuffer byteBuffer = STBImage.stbi_load(filePath, new int[] {width}, new int[] {height}, new int[] {numChannels}, 0);
        return new ImageTexture(width, height, glType, layer, byteBuffer);
    }

    private static int getGlType(final int imageIoType) {
        if (imageIoType == BufferedImage.TYPE_3BYTE_BGR) {
            return GL43.GL_RGB; //TODO - Seems kinda sus but works?
        }
        if (imageIoType == BufferedImage.TYPE_4BYTE_ABGR) {
            return GL43.GL_RGBA;
        }
        throw new RuntimeException("Unsupported image format: " + imageIoType);
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
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D, 0, glType, width, height, 0,
                glType, GL43.GL_UNSIGNED_BYTE, data);
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
