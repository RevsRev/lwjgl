package github.com.rev;

import github.com.rev.gl.texture.buffered.BufferedSwappingTexture;
import github.com.rev.gl.texture.buffered.BufferedTexture;
import github.com.rev.gl.texture.buffered.BufferedTextureImpl;
import github.com.rev.gl.texture.buffered.BufferedTextures;
import github.com.rev.gl.uniform.Uniform;
import github.com.rev.util.ShaderUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public final class DynamicV2 extends WindowedProgram
{
    private boolean resize = false;

    private final String bootstrapFragmentShaderResource;
    private final String dynamicFragmentShaderResource;
    private final String renderFramentShaderResource;

    private final Collection<Uniform> dynamicNonConstantUniforms;
    private final Collection<Uniform> dynamicConstantUniforms;

    /* *****************************
                VERTICES
     ******************************/

    private static final float[] QUAD_VERTICES = {
            // (x, y) , (texX, texY)
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    /* *****************************
            OPENGL RESOURCES
     ******************************/

    //Framebuffer
    private int fbo;
    private int rbo;

    //Virtual Array Objects
    private int bootstrapVao;
    private int dynamicVao;
    private int renderVao;

    //Shaders
    private int bootstrapShaderProgram;
    private int dynamicShaderProgram;
    private int renderShaderProgram;

    //Textures & Layers
    private final String[] layerUniformNames;
    private final BufferedSwappingTexture bufferedSwappingTexture;

    public DynamicV2(String title, String bootstrapFragmentShaderResource, String dynamicFragmentShaderResource,
                     String renderFramentShaderResource, Collection<Uniform> uniforms, String[] layerUniformNames) {
        super(title);
        this.bootstrapFragmentShaderResource = bootstrapFragmentShaderResource;
        this.dynamicFragmentShaderResource = dynamicFragmentShaderResource;
        this.renderFramentShaderResource = renderFramentShaderResource;
        this.layerUniformNames = layerUniformNames;

        List<BufferedTexture> primary = new ArrayList<>();
        List<BufferedTexture> secondary = new ArrayList<>();
        for (int i = 0; i < layerUniformNames.length; i++) {
            BufferedTexture tPrimary = new BufferedTextureImpl(layerUniformNames[i], i, i);
            BufferedTexture tSecondary = new BufferedTextureImpl(layerUniformNames[i], i, i);
            primary.add(tPrimary);
            secondary.add(tSecondary);
        }
        this.bufferedSwappingTexture = new BufferedSwappingTexture(new BufferedTextures(primary), new BufferedTextures(secondary));

        this.dynamicConstantUniforms = uniforms.stream().filter(Uniform::isConstant).collect(Collectors.toSet());
        this.dynamicNonConstantUniforms = uniforms.stream().filter(u -> !u.isConstant()).collect(Collectors.toSet());
    }

    @Override
    public void init() {
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        bufferedSwappingTexture.init(width, height);

        fbo = GL43.glGenFramebuffers();
        rbo = GL43.glGenRenderbuffers();

        setupFramebuffer();

        /* *****************************
                BOOTSTRAP PROGRAM
        ******************************/

        bootstrapVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(bootstrapVao);

        int bootstrapVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, bootstrapVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        bootstrapShaderProgram = ShaderUtils.setupShaderProgram(
                "dynamic/shaders/vertex/bootstrap.vert",
                bootstrapFragmentShaderResource
        );

        /* *****************************
                DYNAMIC PROGRAM
        ******************************/

        dynamicVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(dynamicVao);

        int dynamicVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, dynamicVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        dynamicShaderProgram = ShaderUtils.setupShaderProgram(
                "dynamic/shaders/vertex/dynamic.vert",
                dynamicFragmentShaderResource
        );

        GL43.glUseProgram(dynamicShaderProgram);
        for (Uniform dynamicConstantUniform : dynamicConstantUniforms) {
            dynamicConstantUniform.bindForReading(dynamicShaderProgram);
        }

        /* *****************************
                RENDER PROGRAM
        ******************************/

        renderVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(renderVao);

        int renderVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, renderVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        renderShaderProgram = ShaderUtils.setupShaderProgram(
                "dynamic/shaders/vertex/render.vert",
                renderFramentShaderResource
        );

        doBootstrap();
    }

    @Override
    public void run() {
        if (resize) {
            resize();
            resize = false;
            return;
        }

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        GL43.glUseProgram(dynamicShaderProgram);

        for (Uniform nonConstantUniform : dynamicNonConstantUniforms) {
            nonConstantUniform.bindForReading(dynamicShaderProgram);
        }

        bufferedSwappingTexture.bindForReading(dynamicShaderProgram);
        GL43.glDrawBuffers(bufferedSwappingTexture.bindForWriting());

        GL43.glBindVertexArray(dynamicVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glUseProgram(renderShaderProgram);
        bufferedSwappingTexture.bindForReading(renderShaderProgram);

        GL43.glBindVertexArray(renderVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6);
        GL43.glDrawBuffers(GL43.GL_FRONT);

        bufferedSwappingTexture.swap();
    }

    @Override
    public void setupCallbacks(long window) {
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action != GLFW_RELEASE)
                    return;
                if (key == GLFW_KEY_ESCAPE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(
                window,
                getGlfwFramebufferSizeCallback()
        );
    }

    private GLFWFramebufferSizeCallbackI getGlfwFramebufferSizeCallback() {
        return (win, width, height) -> {
            this.width = width;
            this.height = height;
            this.resize = true;
        };
    }

    private void setupFramebuffer() {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        GL43.glBindRenderbuffer(GL43.GL_RENDERBUFFER, rbo);
        GL43.glRenderbufferStorage(GL43.GL_RENDERBUFFER, GL43.GL_DEPTH24_STENCIL8, width, height);
        GL43.glFramebufferRenderbuffer(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_STENCIL_ATTACHMENT, GL43.GL_RENDERBUFFER,
                rbo);

        if (GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER) != GL43.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer was not completed, error code: " + GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER));
        }
    }

    private void resize() {
        GL43.glViewport(0, 0, this.width, this.height);

        setupFramebuffer();
        bufferedSwappingTexture.resize(width, height);

        doBootstrap();
    }

    private void doBootstrap() {
//        int bootstrapFbo = bufferedSwappingTexture.isSwap() ? fboTwo : fbo;
        int bootstrapFbo = fbo;
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, bootstrapFbo);
        GL43.glDrawBuffers(bufferedSwappingTexture.bindForWriting());
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        GL43.glUseProgram(bootstrapShaderProgram);
        GL43.glBindVertexArray(bootstrapVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        bufferedSwappingTexture.swap();
    }

}