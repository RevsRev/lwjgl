package github.com.rev.gl.scene;

import github.com.rev.WindowedProgram;
import github.com.rev.gl.shader.ShaderProgram;
import github.com.rev.gl.texture.buffered.BufferedTexture;
import github.com.rev.gl.texture.buffered.BufferedTextureImpl;
import github.com.rev.gl.texture.image.ImageTexture;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL43.glBindFramebuffer;
import static org.lwjgl.opengl.GL43.glBindVertexArray;
import static org.lwjgl.opengl.GL43.glGenVertexArrays;
import static org.lwjgl.opengl.GL43.glViewport;

public final class Scene extends WindowedProgram {

    private static final float[] SQUARE_VERTICES = new float[]{
            // positions // colors // texture coords
            0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
            0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom left
            -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f // top left
    };
    private static int[] INDICES = {
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    };

    private int vao;
    private int vbo;
    private int ebo;
    private ShaderProgram shader;
    private ImageTexture texture;

    public Scene(String title) {
        super(title);
    }

    @Override
    public void init() {
        createCapabilities();
        glViewport(0, 0, width, height);

        glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = GL43.glGenBuffers();
        ebo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, SQUARE_VERTICES, GL43.GL_STATIC_DRAW);

        GL43.glBindBuffer(GL43.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL43.glBufferData(GL43.GL_ELEMENT_ARRAY_BUFFER, INDICES, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 8 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 3, GL43.GL_FLOAT, false, 8 * 4, 3 * 4);
        GL43.glEnableVertexAttribArray(2);
        GL43.glVertexAttribPointer(2, 2, GL43.GL_FLOAT, false, 8 * 4, 6 * 4);

        shader = new ShaderProgram("scene/shaders/vertex/scene.vert", "scene/shaders/fragment/scene.frag");

        String texturePath = new File("src/main/resources/scene/textures/matrix.jpg").getAbsolutePath();

//        ByteBuffer byteBuffer;
//
//        try (MemoryStack stack = MemoryStack.stackPush()) {
//            IntBuffer x = stack.mallocInt(1);
//            IntBuffer y = stack.mallocInt(1);
//            IntBuffer n = stack.mallocInt(3);
//            byteBuffer = STBImage.stbi_load(texturePath, x, y , n, 0);
//        }
        ByteBuffer byteBuffer =
                STBImage.stbi_load(texturePath, new int[] {512}, new int[] {512}, new int[] {3}, 0);

        System.out.println(byteBuffer.isDirect());
        System.out.println(byteBuffer.order());

        texture = new ImageTexture(512, 512, 0, byteBuffer);
        texture.init();
        texture.bindForReading();

        shader.init();
    }

    @Override
    public void run() {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        shader.use();
        GL43.glBindVertexArray(vao);
        texture.bindForReading();

        GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void setupCallbacks(long window) {

    }
}
