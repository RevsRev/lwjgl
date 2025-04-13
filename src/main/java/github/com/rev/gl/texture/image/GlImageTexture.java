package github.com.rev.gl.texture.image;

import github.com.rev.gl.shader.ShaderReadable;
import lombok.Getter;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GlImageTexture {

    private final String name;

    @Getter
    private final ImageTexture imageTexture;

    @Getter
    private int texId;

    @Getter
    private boolean initialized = false;

    private static final Map<String, GlImageTexture> TEXTURE_CACHE = new ConcurrentHashMap<>();

    private GlImageTexture(final ImageTexture imageTexture, String name) {
        this.imageTexture = imageTexture;
        this.name = name;
    }

    public static GlImageTexture fromFile(final String filePath) {
        if (TEXTURE_CACHE.containsKey(filePath)) {
            return TEXTURE_CACHE.get(filePath);
        }

        GlImageTexture glImageTexture = new GlImageTexture(ImageTexture.fromFile(filePath), filePath);
        TEXTURE_CACHE.put(filePath, glImageTexture);
        return glImageTexture;
    }

    public void init() {
        if (initialized) {
            return;
        }

        texId = GL43.glGenTextures();
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texId);

        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_S, GL43.GL_REPEAT);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_T, GL43.GL_REPEAT);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_LINEAR);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_LINEAR);
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D, 0, imageTexture.glType, imageTexture.width, imageTexture.height, 0,
                imageTexture.glType, GL43.GL_UNSIGNED_BYTE, imageTexture.data);
        GL43.glGenerateMipmap(GL43.GL_TEXTURE_2D);

        initialized = true;
    }
}
