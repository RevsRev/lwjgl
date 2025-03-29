package github.com.rev;

import github.com.rev.gl.texture.SwappingTexture;
import github.com.rev.gl.texture.Texture;
import github.com.rev.gl.texture.Textures;
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
    private int fboTwo;
    private int rboTwo;

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
    private final SwappingTexture swappingTexture;

    public DynamicV2(String title, String bootstrapFragmentShaderResource, String dynamicFragmentShaderResource,
                     String renderFramentShaderResource, Collection<Uniform> uniforms, String[] layerUniformNames) {
        super(title);
        this.bootstrapFragmentShaderResource = bootstrapFragmentShaderResource;
        this.dynamicFragmentShaderResource = dynamicFragmentShaderResource;
        this.renderFramentShaderResource = renderFramentShaderResource;
        this.layerUniformNames = layerUniformNames;

        List<Texture> primary = new ArrayList<>();
        List<Texture> secondary = new ArrayList<>();
        for (int i = 0; i < layerUniformNames.length; i++) {
            Texture tPrimary = new Texture(i);
            Texture tSecondary = new Texture(i);
            primary.add(tPrimary);
            secondary.add(tSecondary);
        }
        this.swappingTexture = new SwappingTexture(new Textures(primary), new Textures(secondary));

        this.dynamicConstantUniforms = uniforms.stream().filter(Uniform::isConstant).collect(Collectors.toSet());
        this.dynamicNonConstantUniforms = uniforms.stream().filter(u -> !u.isConstant()).collect(Collectors.toSet());
    }

    @Override
    public void init() {
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        swappingTexture.init(width, height);

        fbo = GL43.glGenFramebuffers();
        rbo = GL43.glGenRenderbuffers();

        setupFramebuffer(fbo, rbo);

        fboTwo = GL43.glGenFramebuffers();
        rboTwo = GL43.glGenRenderbuffers();

        setupFramebuffer(fboTwo, rboTwo);

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
        for (int i = 0; i < layerUniformNames.length; i++) {
            GL43.glUniform1i(GL43.glGetUniformLocation(dynamicShaderProgram, layerUniformNames[i]), i);
        }

        for (Uniform dynamicConstantUniform : dynamicConstantUniforms) {
            dynamicConstantUniform.bind(dynamicShaderProgram);
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

        GL43.glUseProgram(renderShaderProgram);
        GL43.glUniform1i(GL43.glGetUniformLocation(renderShaderProgram, "screenTexture"), 0);

        doBootstrap();
    }

    @Override
    public void run() {
        if (resize) {
            resize();
            resize = false;
            return;
        }

        int fboInUse = swappingTexture.isSwap() ? fboTwo : fbo;

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fboInUse);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);
        GL43.glUseProgram(dynamicShaderProgram);

        for (Uniform nonConstantUniform : dynamicNonConstantUniforms) {
            nonConstantUniform.bind(dynamicShaderProgram);
        }

        GL43.glDrawBuffers(swappingTexture.bindForWriting());
        swappingTexture.bindForReading();

        GL43.glBindVertexArray(dynamicVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);
        GL43.glUseProgram(renderShaderProgram);
        swappingTexture.bindForReading();

        GL43.glBindVertexArray(renderVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6);

        swappingTexture.swap();
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

    private void setupFramebuffer(int fbo, int rbo) {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        GL43.glBindRenderbuffer(GL43.GL_RENDERBUFFER, rbo);
        GL43.glRenderbufferStorage(GL43.GL_RENDERBUFFER, GL43.GL_DEPTH24_STENCIL8, width, height);
        GL43.glFramebufferRenderbuffer(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_STENCIL_ATTACHMENT, GL43.GL_RENDERBUFFER,
                rbo);

        if (GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER) != GL43.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer was not completed");
        }
    }

    private void resize() {
        GL43.glViewport(0, 0, this.width, this.height);

        setupFramebuffer(fbo, rbo);
        setupFramebuffer(fboTwo, rboTwo);
        swappingTexture.resize(width, height);

        doBootstrap();
    }

    private void doBootstrap() {
        int bootstrapFbo = swappingTexture.isSwap() ? fboTwo : fbo;
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, bootstrapFbo);
        GL43.glDrawBuffers(swappingTexture.bindForWriting());
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        GL43.glUseProgram(bootstrapShaderProgram);
        GL43.glBindVertexArray(bootstrapVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        swappingTexture.swap();
    }

}