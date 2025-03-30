package github.com.rev;

import github.com.rev.util.ShaderUtils;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;

public final class Fractal extends WindowedProgram
{
    //View settings
    private double previousCoordMouseX = 0.0f;
    private double previousCoordMouseY = 0.0f;
    @Setter
    private double coordOriginX = 0.0f;
    @Setter
    private double coordOriginY = 0.0f;
    @Setter
    private double coordXWidth = 2.0f;
    @Setter
    private double coordYWidth = 2.0f;

    @Getter
    private double coordMouseClickX;
    @Getter
    private double coordMouseClickY;

    @Setter
    private Color backgroundColor = new Color(10, 3, 61, 255);
    @Setter
    private Color setColor = new Color(69, 103, 179, 255);

    private final float ZOOM_SENSITIVITY = 0.1f;
    private double coordMouseX;
    private double coordMouseY;

    //Additional parameter injection
    private final Map<String, Supplier<Double>> dynamicShaderProgramUniformFloats;
    private final Map<String, Integer> dynamicShaderProgramUniformStringToId = new HashMap<>();

    //Iterations
    private double globalZoom = 1.0;
    private int maxIterations = 50;
    private final Function<Double, Integer> iterationsFunc;

    private final String fractalFragmentShaderResourcePath;

    private static final float[] QUAD_VERTICES = {
            // (x, y) , (texX, texY)
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final float[] SQUARE_VERTICES = {
            1.0f, 1.0f, 0.0f, // top right
            1.0f, -1.0f, 0.0f, // bottom right
            -1.0f, -1.0f, 0.0f, // bottom left
            -1.0f, 1.0f, 0.0f // top left
    };
    private static final int[] SQUARE_INDICES = {
      0, 1, 3, //first triangle
      1, 2, 3 //second triangle
    };

    //OpenGL stuff for managing state
    private int fbo;
    private int rbo;
    private int fractalShaderProgram;
    private int fractalVao;
    private int coordinateInfoLocation;
    private int maxIterationsLocation;
    private int renderShaderProgram;
    private int renderVao;
    private int textureIdentifier;

    private boolean refreshScreenSize = false;

    public Fractal(String title, String fractalFragmentShaderResourcePath) {
        this(title, fractalFragmentShaderResourcePath, Collections.emptyMap());
    }
    public Fractal(final String title,
                   final String fractalFragmentShaderResourcePath,
                   final Map<String, Supplier<Double>> dynamicShaderProgramUniformFloats) {
        this(title, fractalFragmentShaderResourcePath, dynamicShaderProgramUniformFloats, zoom -> Math.max(50, (int)(50 * Math.pow(zoom, 0.1))));
    }
    public Fractal(final String title,
                   final String fractalFragmentShaderResourcePath,
                   final Map<String, Supplier<Double>> dynamicShaderProgramUniformFloats,
                   final Function<Double, Integer> iterationsFunc) {
        super(title);
        this.fractalFragmentShaderResourcePath = fractalFragmentShaderResourcePath;
        this.dynamicShaderProgramUniformFloats = dynamicShaderProgramUniformFloats;
        this.iterationsFunc = iterationsFunc;
        maxIterations = iterationsFunc.apply(globalZoom);
    }

    @Override
    public void init() {
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        fbo = GL43.glGenFramebuffers();
        textureIdentifier = GL43.glGenTextures();
        rbo = GL43.glGenRenderbuffers();

        setupFramebuffer(fbo, textureIdentifier, rbo);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        fractalVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(fractalVao);

        int fractalVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, fractalVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, SQUARE_VERTICES, GL43.GL_STATIC_DRAW);
        int fractalEbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ELEMENT_ARRAY_BUFFER, fractalEbo);
        GL43.glBufferData(GL43.GL_ELEMENT_ARRAY_BUFFER, SQUARE_INDICES, GL43.GL_STATIC_DRAW);

        int fractalVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER,
                "fractal/shaders/vertex/fractal.vert");
        int fractalFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                fractalFragmentShaderResourcePath);

        fractalShaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(fractalShaderProgram, fractalVertexShader);
        GL43.glAttachShader(fractalShaderProgram, fractalFragmentShader);
        GL43.glLinkProgram(fractalShaderProgram);

        int[] linkStatus = new int[1];
        GL43.glGetProgramiv(fractalShaderProgram, GL43.GL_LINK_STATUS, linkStatus);
        if (linkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(fractalShaderProgram));
        }

        GL43.glUseProgram(fractalShaderProgram);

        for (String uniformName : dynamicShaderProgramUniformFloats.keySet()) {
            dynamicShaderProgramUniformStringToId.put(uniformName, GL43.glGetUniformLocation(fractalShaderProgram, uniformName));
        }

        int backgroundColorShaderLocation = GL43.glGetUniformLocation(fractalShaderProgram, "backgroundColor");
        int setColorShaderLocation = GL43.glGetUniformLocation(fractalShaderProgram, "setColor");

        float[] backgroundColorRGB = backgroundColor.getRGBColorComponents(new float[3]);
        float[] setColorRGB = setColor.getRGBColorComponents(new float[3]);

        GL43.glUniform4f(backgroundColorShaderLocation,
                backgroundColorRGB[0],
                backgroundColorRGB[1],
                backgroundColorRGB[2],
                1.0f);
        GL43.glUniform4f(setColorShaderLocation,
                setColorRGB[0],
                setColorRGB[1],
                setColorRGB[2],
                1.0f);

        coordinateInfoLocation = GL43.glGetUniformLocation(fractalShaderProgram, "coordInfo");
        GL43.glUniform4d(coordinateInfoLocation, coordOriginX, coordOriginY, coordXWidth, coordYWidth);

        maxIterationsLocation = GL43.glGetUniformLocation(fractalShaderProgram, "maxIterations");
        GL43.glUniform1i(maxIterationsLocation, maxIterations);

        // linked, so we don't need anymore :)
        GL43.glDeleteShader(fractalVertexShader);
        GL43.glDeleteShader(fractalFragmentShader);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 3 * 4, 0);

        //Now set up the render stuff
        renderVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(renderVao);

        int renderVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, renderVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        int renderVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER,
                "fractal/shaders/vertex/render.vert");
        int renderFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                "fractal/shaders/fragment/render.frag");

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

        glfwSwapInterval(1);
    }

    @Override
    public void run() {
        if (refreshScreenSize) {
            GL43.glViewport(0, 0, this.width, this.height);
            setupFramebuffer(fbo, textureIdentifier, rbo);
            refreshScreenSize = false;
        }

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
        GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
        GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!

        GL43.glUseProgram(fractalShaderProgram);
        GL43.glBindVertexArray(fractalVao);

        //Set any additional uniforms
        for (Map.Entry<String, Supplier<Double>> uniformNameAndFunc : dynamicShaderProgramUniformFloats.entrySet()) {
            String uniformName = uniformNameAndFunc.getKey();
            Supplier<Double> supplier = uniformNameAndFunc.getValue();
            double value = supplier.get();
            GL43.glUniform1d(dynamicShaderProgramUniformStringToId.get(uniformName), value);
        }

        GL43.glUniform4d(coordinateInfoLocation, coordOriginX, coordOriginY, coordXWidth, coordYWidth);
        GL43.glUniform1i(maxIterationsLocation, maxIterations);
        GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);
        GL43.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        GL43.glUseProgram(renderShaderProgram);
        GL43.glBindVertexArray(renderVao);
        GL43.glDisable(GL43.GL_DEPTH_TEST);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, textureIdentifier);

        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0 , 6);
    }

    @Override
    public void setupCallbacks(long window) {
        glfwSetKeyCallback(window, getKeyCallback());
        glfwSetScrollCallback(window, getScrollCallback());
        glfwSetCursorPosCallback(window, getCursorPosCallback());
        glfwSetMouseButtonCallback(window, getMouseButtonCallback());
        glfwSetFramebufferSizeCallback(
                window,
                getGlfwFramebufferSizeCallback(fbo, textureIdentifier, rbo)
        );
    }

    private GLFWKeyCallback getKeyCallback() {
        return new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action != GLFW_RELEASE)
                    return;
                if (key == GLFW_KEY_ESCAPE)
                    glfwSetWindowShouldClose(window, true);
            }
        };
    }

    private GLFWMouseButtonCallbackI getMouseButtonCallback() {
        return (window, button, action, mods) ->
        {
            if (button == GLFW_RELEASE) {
                coordMouseClickX = coordMouseX;
                coordMouseClickY = coordMouseY;
            }
        };
    }

    private GLFWFramebufferSizeCallbackI getGlfwFramebufferSizeCallback(int fbo, int textureIdentifier, int rbo) {
        return (win, width, height) -> {
            this.width = width;
            this.height = height;
            this.refreshScreenSize = true;
        };
    }

    private void setupFramebuffer(int fbo, int textureIdentifier, int rbo) {
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);

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

        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_LINEAR);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_LINEAR);

        GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, GL43.GL_COLOR_ATTACHMENT0, GL43.GL_TEXTURE_2D,
                textureIdentifier, 0);

        GL43.glBindRenderbuffer(GL43.GL_RENDERBUFFER, rbo);
        GL43.glRenderbufferStorage(GL43.GL_RENDERBUFFER, GL43.GL_DEPTH24_STENCIL8, width, height);
        GL43.glFramebufferRenderbuffer(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_STENCIL_ATTACHMENT, GL43.GL_RENDERBUFFER, rbo);

        if (GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER) != GL43.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer was not completed");
        }
    }

    private GLFWScrollCallbackI getScrollCallback() {
        return (window, xOffset, yOffset) ->
        {
            float zoom = (float) Math.exp(yOffset * ZOOM_SENSITIVITY);
            globalZoom *= zoom;

            coordOriginX = coordMouseX + (coordOriginX - coordMouseX) / zoom;
            coordOriginY = coordMouseY + (coordOriginY - coordMouseY) / zoom;

            coordXWidth *= 1/zoom;
            coordYWidth *= 1/zoom;

            maxIterations = iterationsFunc.apply(globalZoom);
//            System.out.printf("Magnification: %s%n", Math.log10(globalZoom));
        };
    }

    private GLFWCursorPosCallbackI getCursorPosCallback() {
        return (window, xpos, ypos) ->
        {
            previousCoordMouseX = coordMouseX;
            previousCoordMouseY = coordMouseY;

            double screenMouseX = (2 * (xpos - width /2.0)/ width);
            double screenMouseY = - (2 * (ypos - height /2.0)/ height);
            coordMouseX = coordOriginX + screenMouseX * coordXWidth;
            coordMouseY = coordOriginY + screenMouseY * coordYWidth;

            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                coordOriginX = coordOriginX + (previousCoordMouseX - coordMouseX);
                coordOriginY = coordOriginY + (previousCoordMouseY - coordMouseY);
            }
        };
    }

}