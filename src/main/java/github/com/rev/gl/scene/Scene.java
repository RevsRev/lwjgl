package github.com.rev.gl.scene;

import github.com.rev.WindowedProgram;
import github.com.rev.gl.scene.item.SimpleItem;
import github.com.rev.gl.scene.light.DirectionalLight;
import github.com.rev.gl.scene.light.PointLight;
import github.com.rev.gl.scene.light.SpotLight;
import github.com.rev.gl.shader.Uniforms;
import github.com.rev.gl.texture.LayerManager;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL43.glBindFramebuffer;
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


    private final List<DirectionalLight> directionalLights = new ArrayList<>();
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();

    private final Camera camera = new Camera();
    private final CameraController cameraController = new CameraController(camera);
    private SimpleItem cube;
    private SimpleItem pointLightCube;
    private Uniforms sceneUniforms;

    public Scene(String title) {
        super(title);
    }

    @Override
    public void init() {
        createCapabilities();
        glViewport(0, 0, width, height);

        GL43.glEnable(GL43.GL_DEPTH_TEST);

        glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        PointLight pointLight = new PointLight(new Axes(), new Position(new Vector3f(2.0f, 3.0f, 0.0f)));
        pointLights.add(pointLight);
        directionalLights.add(new DirectionalLight());

        cube = new SimpleItem.Builder(
                CUBE_VERTICES,
                "scene/shaders/vertex/simple_item.vert",
                "scene/shaders/fragment/simple_item_simple_material.frag")
                .addAmbientTexture("src/main/resources/scene/textures/container.jpg")
                .addDiffuseTexture("src/main/resources/scene/textures/container.jpg")
                .addSpecularTexture("src/main/resources/scene/textures/container.jpg")
                .build(new LayerManager());

        pointLightCube = new SimpleItem.Builder(
                CUBE_VERTICES,
                "scene/shaders/vertex/point_light.vert",
                "scene/shaders/fragment/point_light.frag")
                .setPoint(pointLight)
                .build(new LayerManager());
        pointLightCube.point.setScale(0.1f);

        sceneUniforms = new Uniforms();

        sceneUniforms.addPrimitiveUniform("view", camera, (id, cam) -> GL43.glUniformMatrix4fv(id, false, cam.getViewFloats()));
        sceneUniforms.addPrimitiveUniform("projection", camera, (id, cam) -> GL43.glUniformMatrix4fv(id, false, cam.getPerspectiveFloats()));

        sceneUniforms.addStructArrayUniform(
                "pointLights", pointLights.toArray(new PointLight[0]), PointLight.structUniforms());
        sceneUniforms.addStructArrayUniform(
                "dirLights", directionalLights.toArray(new DirectionalLight[0]), DirectionalLight.structUniforms()
        );
    }

    @Override
    public void run() {

        long frameStart = System.nanoTime();

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        cube.point.rotate(0.01f, new Vector3f(1,1,1));
        cube.render(sceneUniforms);
        pointLightCube.point.rotate(0.01f, new Vector3f(1,1,1));
        pointLightCube.render(sceneUniforms);

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
