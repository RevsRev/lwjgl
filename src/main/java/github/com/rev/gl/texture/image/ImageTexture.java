package github.com.rev.gl.texture.image;

import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageTexture {

    final int width;
    final int height;
    final int glType;
    final ByteBuffer data;

    private static final Map<String, ImageTexture> TEXTURE_CACHE = new HashMap<>();

    private ImageTexture(int width, int height, int glType, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.glType = glType;
        this.data = data;
    }

    public static ImageTexture fromFile(final String filePath) {

        if (TEXTURE_CACHE.containsKey(filePath)) {
            return TEXTURE_CACHE.get(filePath);
        }

        final int imageIoType;
        try {
            BufferedImage read = ImageIO.read(new File(filePath));
            imageIoType = read.getType();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not read image file '%s'", filePath), e);
        }

        int glType = getGlType(imageIoType);
        int[] x = new int[1];
        int[] y = new int[1];
        int[] numChannels = new int[1];
        STBImage.stbi_info(filePath, x, y, numChannels);
        ByteBuffer byteBuffer = STBImage.stbi_load(filePath, x, y, numChannels, 0);
        ImageTexture imageTexture = new ImageTexture(x[0], y[0], glType, byteBuffer);
        TEXTURE_CACHE.put(filePath, imageTexture);
        return imageTexture;
    }

    private static int getGlType(final int imageIoType) {
        if (imageIoType == BufferedImage.TYPE_3BYTE_BGR) {
            return GL43.GL_RGB; //TODO - Seems kinda sus but works?
        }
        if (imageIoType == BufferedImage.TYPE_4BYTE_ABGR) {
            return GL43.GL_RGBA;
        }
        if (imageIoType == BufferedImage.TYPE_BYTE_GRAY) {
//            return GL43.GL_RED;
//            return GL43.GL_RGBA; //TODO - Broken!
        }
        throw new RuntimeException("Unsupported image format: " + imageIoType);
    }
}
