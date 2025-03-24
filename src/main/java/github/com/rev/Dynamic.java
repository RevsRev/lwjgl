package github.com.rev;

import github.com.rev.util.ShaderUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;

public final class Dynamic extends WindowedProgram
{

    public static final String DEFAULT_DYNAMIC_FRAGMENT_SHADER_LOCATION = "dynamic/shaders/fragment/render.frag";
    private final String dynamicShaderResourceLocation;
    private final String bootstrapShaderResourceLocation;
    private final String renderShaderResourceLocation;
    private final Map<String, Function<Long, Float>> dynamicShaderProgramUniformFloats;
    private final Map<String, Integer> dynamicShaderProgramUniformStringToId = new HashMap<>();
    private final Optional<Runnable> sleepStrategy;

    boolean swap = true;
    boolean shouldResize = false;
    long prevTime = System.nanoTime();
    long time = prevTime;

    private static final float[] QUAD_VERTICES = {
            // (x, y) , (texX, texY)
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };
    private int textureIdentifier;
    private int textureIdentifierSwap;
    private int fbo;
    private int dynamicShaderProgram;
    private int dynamicVao;
    private int renderShaderProgram;
    private int renderVao;
    private int otherTextureIdentifier;
    private int rbo;
    private int bootstrapShaderProgram;
    private int bootstrapVao;

    public Dynamic(final String title,
                   final String dynamicShaderResourceLocation,
                   final String bootstrapShaderResourceLocation) {
        this(title,
                dynamicShaderResourceLocation,
                bootstrapShaderResourceLocation,
                Collections.emptyMap(),
                Optional.empty());
    }
    public Dynamic(final String title,
                   final String dynamicShaderResourceLocation,
                   final String bootstrapShaderResourceLocation,
                   Map<String, Function<Long, Float>> dynamicShaderProgramUniformFloats,
                   Optional<Runnable> sleepStrategy) {
        this(title,
                dynamicShaderResourceLocation,
                bootstrapShaderResourceLocation,
                dynamicShaderProgramUniformFloats,
                sleepStrategy,
                DEFAULT_DYNAMIC_FRAGMENT_SHADER_LOCATION);
    }
    public Dynamic(final String title,
                   final String dynamicShaderResourceLocation,
                   final String bootstrapShaderResourceLocation,
                   Map<String, Function<Long, Float>> dynamicShaderProgramUniformFloats,
                   Optional<Runnable> sleepStrategy,
                   String renderShaderResourceLocation) {
        super(title);
        this.dynamicShaderResourceLocation = dynamicShaderResourceLocation;
        this.bootstrapShaderResourceLocation = bootstrapShaderResourceLocation;
        this.dynamicShaderProgramUniformFloats = dynamicShaderProgramUniformFloats;
        this.sleepStrategy = sleepStrategy;
        this.renderShaderResourceLocation = renderShaderResourceLocation;
    }

    @Override
    public void init() {
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        fbo = GL43.glGenFramebuffers();
        textureIdentifier = GL43.glGenTextures();
        textureIdentifierSwap = GL43.glGenTextures();
        rbo = GL43.glGenRenderbuffers();

        setupFramebuffer();

        setupTexture(textureIdentifier);
        bindTexture(textureIdentifier);
        setupTexture(textureIdentifierSwap);
        bindTexture(textureIdentifierSwap);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        // BOOTSTRAP VAO, VBO SHADERS

        bootstrapVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(bootstrapVao);

        int bootstrapVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, bootstrapVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        int bootstrapVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER,
                "dynamic/shaders/vertex/bootstrap.vert");
        int bootstrapFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                bootstrapShaderResourceLocation);

        bootstrapShaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(bootstrapShaderProgram, bootstrapVertexShader);
        GL43.glAttachShader(bootstrapShaderProgram, bootstrapFragmentShader);
        GL43.glLinkProgram(bootstrapShaderProgram);

        int[] boostrapLinkStatus = new int[1];
        GL43.glGetProgramiv(bootstrapShaderProgram, GL43.GL_LINK_STATUS, boostrapLinkStatus);
        if (boostrapLinkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(bootstrapShaderProgram));
        }

        GL43.glUseProgram(bootstrapShaderProgram);
        GL43.glUniform1i(GL43.glGetUniformLocation(bootstrapShaderProgram, "screenTexture"), 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        // DYNAMIC VAO, EBO, VBO, SHADERS

        dynamicVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(dynamicVao);

        int dynamicVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, dynamicVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        int dynamicVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER, "dynamic/shaders/vertex/dynamic.vert");
        int dynamicFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                dynamicShaderResourceLocation);

        dynamicShaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(dynamicShaderProgram, dynamicVertexShader);
        GL43.glAttachShader(dynamicShaderProgram, dynamicFragmentShader);
        GL43.glLinkProgram(dynamicShaderProgram);

        int[] linkStatus = new int[1];
        GL43.glGetProgramiv(dynamicShaderProgram, GL43.GL_LINK_STATUS, linkStatus);
        if (linkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(dynamicShaderProgram));
        }

        GL43.glUseProgram(dynamicShaderProgram);

        for (String uniformName : dynamicShaderProgramUniformFloats.keySet()) {
            dynamicShaderProgramUniformStringToId.put(uniformName, GL43.glGetUniformLocation(dynamicShaderProgram, uniformName));
        }

        // linked, so we don't need anymore :)
        GL43.glDeleteShader(dynamicVertexShader);
        GL43.glDeleteShader(dynamicFragmentShader);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);


        // RENDER VAO, VBO, SHADERS

        renderVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(renderVao);

        int renderVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, renderVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        int renderVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER,
                "dynamic/shaders/vertex/render.vert");
        int renderFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                renderShaderResourceLocation);

        renderShaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(renderShaderProgram, renderVertexShader);
        GL43.glAttachShader(renderShaderProgram, renderFragmentShader);
        GL43.glLinkProgram(renderShaderProgram);

        int[] renderLinkStatus = new int[1];
        GL43.glGetProgramiv(renderShaderProgram, GL43.GL_LINK_STATUS, renderLinkStatus);
        if (renderLinkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(renderShaderProgram));
        }

        GL43.glUseProgram(renderShaderProgram);
        GL43.glUniform1i(GL43.glGetUniformLocation(renderShaderProgram, "screenTexture"), 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        //Load initial data into the (first) texture specified by textureIdentifier
        doBootstrap(textureIdentifier);

        glfwSwapInterval(1);
    }

    @Override
    public void run()
    {
        if (shouldResize) {
            resize();
            shouldResize = false;
        }


        long deltaT = 10;


        time = System.nanoTime();

        int texOne = swap ? textureIdentifier : textureIdentifierSwap; //The texture I want to read from
        int texTwo = swap ? textureIdentifierSwap : textureIdentifier; //The texture I want to write to

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        bindTexture(texTwo);
        GL43.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
        GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texOne);

        GL43.glUseProgram(dynamicShaderProgram);
        GL43.glBindVertexArray(dynamicVao);

        //Set uniforms we use in the program
        for (Map.Entry<String, Function<Long, Float>> uniformNameAndFunc : dynamicShaderProgramUniformFloats.entrySet()) {
            String uniformName = uniformNameAndFunc.getKey();
            Function<Long, Float> func = uniformNameAndFunc.getValue();
            float value = func.apply(deltaT);
            GL43.glUniform1f(dynamicShaderProgramUniformStringToId.get(uniformName), value);
        }

//            GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        GL43.glUseProgram(renderShaderProgram);
        GL43.glBindVertexArray(renderVao);
        GL43.glDisable(GL43.GL_DEPTH_TEST);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texTwo);
        GL43.glUniform1i(GL43.glGetUniformLocation(renderShaderProgram, "screenTexture"), 0);

        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        swap = !swap;

        deltaT = System.nanoTime() - time;
        sleepStrategy.ifPresent(Runnable::run); //slow the simulation down, but don't increase the step of the simulation
    }

    private void doBootstrap(int textureIdentifier) {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        bindTexture(textureIdentifier);
        GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
        GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!

        GL43.glUseProgram(bootstrapShaderProgram);
        GL43.glBindVertexArray(bootstrapVao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);
    }

    private void setupFramebuffer() {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        GL43.glBindRenderbuffer(GL43.GL_RENDERBUFFER, rbo);
        GL43.glRenderbufferStorage(GL43.GL_RENDERBUFFER, GL43.GL_DEPTH24_STENCIL8, width, height);
        GL43.glFramebufferRenderbuffer(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_STENCIL_ATTACHMENT, GL43.GL_RENDERBUFFER, rbo);

        if (GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER) != GL43.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer was not completed");
        }
    }

    private void setupTexture(int textureIdentifier) {
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, textureIdentifier);
        GL43.glTexImage2D(GL43.GL_TEXTURE_2D,
                0,
                GL43.GL_RGB,
                width,
                height,
                0,
                GL43.GL_RGB,
                GL43.GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
    }

    private void bindTexture(int textureIdentifier) {
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_LINEAR);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_LINEAR);

        GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, GL43.GL_COLOR_ATTACHMENT0, GL43.GL_TEXTURE_2D,
                textureIdentifier, 0);
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
            this.shouldResize = true;
        };
    }

    private void resize() {
        GL43.glViewport(0, 0, this.width, this.height);

        int texture = swap ? textureIdentifier : textureIdentifierSwap; //Bootstrap to the correct texture!
        int otherTexture = swap ? textureIdentifierSwap : textureIdentifier;

        setupFramebuffer();
        setupTexture(texture);
        bindTexture(texture);
        setupTexture(otherTexture);
        bindTexture(otherTexture);

        doBootstrap(texture);
    }
}