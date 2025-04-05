package github.com.rev.gl.scene;

import github.com.rev.WindowedProgram;
import github.com.rev.gl.scene.light.DirectionalLight;
import github.com.rev.gl.scene.light.PointLight;
import github.com.rev.gl.scene.light.SpotLight;
import github.com.rev.gl.shader.ShaderProgram;
import github.com.rev.gl.texture.image.ImageTexture;
import github.com.rev.gl.uniform.UniformPrimative;
import github.com.rev.gl.uniform.UniformStructArray;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
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

    private static final float[] CUBE_VERTICES = {
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
    };

    private int vao;
    private int vbo;
    private int ebo;
    private ShaderProgram shader;
    private ImageTexture texture;

    private Matrix4f model;
    private UniformPrimative uniformModel;

    private final List<DirectionalLight> directionalLights = new ArrayList<>();
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();
    private UniformStructArray<PointLight> pointLightsUniform;
    private UniformPrimative uniformView;
    private UniformPrimative uniformProjection;

    private final Camera camera = new Camera();
    private final CameraController cameraController = new CameraController(camera);
    private UniformStructArray<DirectionalLight> directionalLightsUniform;

    public Scene(String title) {
        super(title);
    }

    @Override
    public void init() {
        createCapabilities();
        glViewport(0, 0, width, height);

        GL43.glEnable(GL43.GL_DEPTH_TEST);

        glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = GL43.glGenBuffers();
        ebo = GL43.glGenBuffers();

        //CUBE
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, CUBE_VERTICES, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 8 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 3, GL43.GL_FLOAT, false, 8 * 4, 3 * 4);
        GL43.glEnableVertexAttribArray(2);
        GL43.glVertexAttribPointer(2, 2, GL43.GL_FLOAT, false, 8 * 4, 6 * 4);

        shader = new ShaderProgram("scene/shaders/vertex/scene.vert", "scene/shaders/fragment/scene.frag");

        String texturePath = new File("src/main/resources/scene/textures/container.jpg").getAbsolutePath();
        ByteBuffer byteBuffer =
                STBImage.stbi_load(texturePath, new int[] {512}, new int[] {512}, new int[] {3}, 0);

        texture = new ImageTexture(512, 512, 0, byteBuffer);
        texture.init();
        texture.bindForReading(shader.getProgramId());

        model = new Matrix4f();
        uniformModel =
                new UniformPrimative("model", false, id -> GL43.glUniformMatrix4fv(id, false, model.get(new float[16])));

        uniformView =
                new UniformPrimative("view", false, id -> GL43.glUniformMatrix4fv(id, false, camera.view.get(new float[16])));

        uniformProjection =
                new UniformPrimative("projection", false, id -> GL43.glUniformMatrix4fv(id, false, camera.perspective.get(new float[16])));

        PointLight pointLight = new PointLight();
        pointLights.add(pointLight);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLights.add(directionalLight);

        pointLightsUniform = new UniformStructArray<>(
                "pointLights",
                true,
                PointLight.structUniforms(),
                pointLights.toArray(new PointLight[0]));

        directionalLightsUniform = new UniformStructArray<>(
                "dirLights",
                true,
                DirectionalLight.structUniforms(),
                directionalLights.toArray(new DirectionalLight[0]));

        shader.addUniform(uniformModel);
        shader.addUniform(uniformView);
        shader.addUniform(uniformProjection);
        shader.addUniform(pointLightsUniform);
        shader.addUniform(directionalLightsUniform);
        shader.init();
    }

    @Override
    public void run() {

        long frameStart = System.nanoTime();

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        model.rotate(0.01f, new Vector3f(1,1,1));
        shader.use();
        GL43.glBindVertexArray(vao);
        texture.bindForReading(shader.getProgramId());

//        GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 36);

        long frameTime = System.nanoTime() - frameStart;
        double dT = (double)(frameTime) / 1000000.0d;
        cameraController.process(dT);
    }

    @Override
    public void setupCallbacks(long window) {
        glfwSetKeyCallback(window, getGlfwKeyCallback());
        glfwSetCursorPosCallback(window, getCursorPosCallback());
    }

    @Override
    public int getCursorMode() {
        return GLFW_CURSOR_DISABLED;
    }

    private GLFWKeyCallback getGlfwKeyCallback() {
        return new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {

                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, true);
                    return;
                }
                cameraController.handleKey(key, scancode, action, mods);
            }
        };
    }

    private GLFWCursorPosCallbackI getCursorPosCallback() {
        return (window, xpos, ypos) ->
        {
            cameraController.handleMouse(xpos, ypos);
        };
    }
}
