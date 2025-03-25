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

public final class DynamicSecondOrder extends WindowedProgram
{

    public static final String DEFAULT_DYNAMIC_FRAGMENT_SHADER_LOCATION = "dynamic/shaders/fragment/render.frag";
    private final String funcDynamicFragmentShaderResourceLocation;
    private final String derivativeDynamicFragmentShaderResourceLocation;
    private final String funcInitialFragmentShaderResourceLocation;
    private final String funcInitialDerivativeFragmentShaderResourceLocation;
    private final String renderShaderResourceLocation;
    private final Map<String, Function<Long, Float>> funcDyanmicShaderProgramUniformFloats;
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

    private int funcFbo;
    private int funcRbo;
    private int funcTextureOne;
    private int funcTextureTwo;

    private int derivativeFbo;
    private int derivativeRbo;
    private int derivativeTextureOne;
    private int derivativeTextureTwo;

    private int funcInitialShaderProgram;
    private int derivativeInitialShaderProgram;
    private int funcDynamicShaderProgram;
    private int derivativeDynamicShaderProgram;
    private int renderShaderProgram;

    private int funcInitialFao;
    private int derivativeInitialFao;
    private int funcDynamicVao;
    private int derivativeDynamicVao;
    private int renderVao;

    private int otherTextureIdentifier;

    public DynamicSecondOrder(final String title,
                              final String funcDynamicFragmentShaderResourceLocation,
                              final String funcInitialFragmentShaderResourceLocation) {
        this(title,
                funcDynamicFragmentShaderResourceLocation,
                funcInitialFragmentShaderResourceLocation,
                Collections.emptyMap(),
                Optional.empty());
    }
    public DynamicSecondOrder(final String title,
                              final String funcDynamicFragmentShaderResourceLocation,
                              final String funcInitialFragmentShaderResourceLocation,
                              Map<String, Function<Long, Float>> funcDyanmicShaderProgramUniformFloats,
                              Optional<Runnable> sleepStrategy) {
        this(title,
                funcDynamicFragmentShaderResourceLocation,
                funcInitialFragmentShaderResourceLocation,
                funcDyanmicShaderProgramUniformFloats,
                sleepStrategy,
                DEFAULT_DYNAMIC_FRAGMENT_SHADER_LOCATION);
    }
    public DynamicSecondOrder(final String title,
                              final String funcDynamicFragmentShaderResourceLocation,
                              final String funcInitialFragmentShaderResourceLocation,
                              Map<String, Function<Long, Float>> funcDyanmicShaderProgramUniformFloats,
                              Optional<Runnable> sleepStrategy,
                              String renderShaderResourceLocation) {
        super(title);
        this.funcDynamicFragmentShaderResourceLocation = funcDynamicFragmentShaderResourceLocation;
        this.derivativeDynamicFragmentShaderResourceLocation = funcDynamicFragmentShaderResourceLocation; //TODO - Add own parameter?
        this.funcInitialFragmentShaderResourceLocation = funcInitialFragmentShaderResourceLocation;
        this. funcInitialDerivativeFragmentShaderResourceLocation = funcInitialFragmentShaderResourceLocation; //TODO - Add own parameter?
        this.funcDyanmicShaderProgramUniformFloats = funcDyanmicShaderProgramUniformFloats;
        this.sleepStrategy = sleepStrategy;
        this.renderShaderResourceLocation = renderShaderResourceLocation;
    }

    @Override
    public void init() {
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        funcFbo = GL43.glGenFramebuffers();
        funcRbo = GL43.glGenRenderbuffers();
        funcTextureOne = GL43.glGenTextures();
        funcTextureTwo = GL43.glGenTextures();

        derivativeFbo = GL43.glGenFramebuffers();
        derivativeRbo = GL43.glGenRenderbuffers();
        derivativeTextureOne = GL43.glGenTextures();
        derivativeTextureTwo = GL43.glGenTextures();

        setupFramebuffer(funcFbo, funcRbo);
        setupFramebuffer(derivativeFbo, derivativeRbo);

        setupTexture(funcTextureOne);
        bindTexture(funcTextureOne);
        setupTexture(funcTextureTwo);
        bindTexture(funcTextureTwo);

        setupTexture(derivativeTextureOne);
        bindTexture(derivativeTextureOne);
        setupTexture(derivativeTextureTwo);
        bindTexture(derivativeTextureTwo);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        /* **************************************
                Initial Function Values
        ***************************************/

        funcInitialFao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(funcInitialFao);

        int funcInitialVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, funcInitialVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        funcInitialShaderProgram = ShaderUtils.setupShaderProgram("dynamic/shaders/vertex/bootstrap.vert",
                funcInitialFragmentShaderResourceLocation);

        GL43.glUseProgram(funcInitialShaderProgram);

        GL43.glUniform1i(GL43.glGetUniformLocation(funcInitialShaderProgram, "screenTexture"), 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        /* **************************************
              Initial Function Derivatives
        ***************************************/

        derivativeInitialFao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(derivativeInitialFao);

        int derivativeInitialFbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, derivativeInitialFbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        derivativeInitialShaderProgram = ShaderUtils.setupShaderProgram("dynamic/shaders/vertex/bootstrap.vert",
                funcInitialDerivativeFragmentShaderResourceLocation);

        GL43.glUseProgram(derivativeInitialShaderProgram);

        GL43.glUniform1i(GL43.glGetUniformLocation(derivativeInitialShaderProgram, "screenTexture"), 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        /* **************************************
              Dynamic Function Values
        ***************************************/

        funcDynamicVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(funcDynamicVao);

        int funcDynamicVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, funcDynamicVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        funcDynamicShaderProgram = ShaderUtils.setupShaderProgram("dynamic/shaders/vertex/dynamic.vert",
                funcDynamicFragmentShaderResourceLocation);
        GL43.glUseProgram(funcDynamicShaderProgram);

        for (String uniformName : funcDyanmicShaderProgramUniformFloats.keySet()) {
            dynamicShaderProgramUniformStringToId.put(uniformName, GL43.glGetUniformLocation(funcDynamicShaderProgram, uniformName));
        }

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        /* **************************************
              Dynamic Derivative Values
        ***************************************/

        derivativeDynamicVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(derivativeDynamicVao);

        int derivativeDynamicVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, derivativeDynamicVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        derivativeDynamicShaderProgram = ShaderUtils.setupShaderProgram("dynamic/shaders/vertex/dynamic.vert",
                derivativeDynamicFragmentShaderResourceLocation);
        GL43.glUseProgram(derivativeDynamicShaderProgram);

        // TODO - Similar uniforms for derivative...
//        for (String uniformName : funcDyanmicShaderProgramUniformFloats.keySet()) {
//            dynamicShaderProgramUniformStringToId.put(uniformName, GL43.glGetUniformLocation(derivativeDynamicShaderProgram, uniformName));
//        }

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        /* **************************************
                  Rendering to screen
        ***************************************/

        renderVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(renderVao);

        int renderVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, renderVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        renderShaderProgram = ShaderUtils.setupShaderProgram("dynamic/shaders/vertex/render.vert", renderShaderResourceLocation);

        GL43.glUseProgram(renderShaderProgram);
        GL43.glUniform1i(GL43.glGetUniformLocation(renderShaderProgram, "screenTexture"), 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 2, GL43.GL_FLOAT, false, 4 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 4 * 4, 2 * 4);

        //Load initial data into the (first) texture specified by textureIdentifier
        doBootstrap(funcTextureOne, funcFbo, funcInitialFao, funcInitialShaderProgram);
        doBootstrap(derivativeTextureOne, derivativeFbo, derivativeInitialFao, derivativeInitialShaderProgram);

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

        int funcTexOne = swap ? funcTextureOne : funcTextureTwo; //The texture I want to read from
        int funcTexTwo = swap ? funcTextureTwo : funcTextureOne; //The texture I want to write to

        int derivativeTexOne = swap ? derivativeTextureOne : derivativeTextureTwo; //The texture I want to read from
        int derivativeTexTwo = swap ? derivativeTextureTwo : derivativeTextureOne; //The texture I want to write to

        //Step one - use the func and derivative values from the previous buffers to calculate the func at the next step
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, funcFbo);
        bindTexture(funcTexTwo);
        GL43.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
        GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, funcTexOne);

        GL43.glUseProgram(funcDynamicShaderProgram);
        GL43.glBindVertexArray(funcDynamicVao);

        //Set uniforms we use in the program
        for (Map.Entry<String, Function<Long, Float>> uniformNameAndFunc : funcDyanmicShaderProgramUniformFloats.entrySet()) {
            String uniformName = uniformNameAndFunc.getKey();
            Function<Long, Float> func = uniformNameAndFunc.getValue();
            float value = func.apply(deltaT);
            GL43.glUniform1f(dynamicShaderProgramUniformStringToId.get(uniformName), value);
        }

//            GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        //Step two - use the previous and next func values to calculate the next derivative value

        //Step three - display the next function to the main window

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        GL43.glUseProgram(renderShaderProgram);
        GL43.glBindVertexArray(renderVao);
        GL43.glDisable(GL43.GL_DEPTH_TEST);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, funcTexTwo);
        GL43.glUniform1i(GL43.glGetUniformLocation(renderShaderProgram, "screenTexture"), 0);

        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);

        swap = !swap;

        deltaT = System.nanoTime() - time;
        sleepStrategy.ifPresent(Runnable::run); //slow the simulation down, but don't increase the step of the simulation
    }

    private void doBootstrap(int textureIdentifier, int fbo, int fao, int shaderProgram) {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        bindTexture(textureIdentifier);
        GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
        GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!

        GL43.glUseProgram(shaderProgram);
        GL43.glBindVertexArray(fao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);
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

        int funcTexture = swap ? funcTextureOne : funcTextureTwo; //Bootstrap to the correct texture!
        int funcOtherTexture = swap ? funcTextureTwo : funcTextureOne;
        int derivativeTexture = swap ? derivativeTextureOne : derivativeTextureTwo; //Bootstrap to the correct texture!
        int derivativeOtherTexture = swap ? derivativeTextureTwo : derivativeTextureOne;

        setupFramebuffer(funcFbo, funcRbo);
        setupTexture(funcTexture);
        bindTexture(funcTexture);
        setupTexture(funcOtherTexture);
        bindTexture(funcOtherTexture);

        setupFramebuffer(derivativeFbo, derivativeRbo);
        setupTexture(derivativeTexture);
        bindTexture(derivativeTexture);
        setupTexture(derivativeOtherTexture);
        bindTexture(derivativeOtherTexture);

        doBootstrap(funcTexture, funcFbo, funcInitialFao, funcInitialShaderProgram);
        doBootstrap(derivativeTexture, derivativeFbo, derivativeInitialFao, derivativeInitialShaderProgram);
    }
}