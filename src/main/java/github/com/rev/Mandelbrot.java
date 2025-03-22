package github.com.rev;

import github.com.rev.util.ShaderUtils;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.awt.Color;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Mandelbrot
{
    private int width = 800;
    private int height = 600;

    //View settings
    private double previousCoordMouseX = 0.0f;
    private double previousCoordMouseY = 0.0f;
    private double coordOriginX = 0.0f;
    private double coordOriginY = 0.0f;
    private double coordXWidth = 2.0f;
    private double coordYWidth = 2.0f;

    private final float ZOOM_SENSITIVITY = 0.1f;
    private double coordMouseX;
    private double coordMouseY;

    //Iterations
    private double globalZoom = 1.0;
    private int maxIterations = 50;

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

    public void run()
    {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        long window = glfwCreateWindow(width, height, "Mandelbrot", NULL, NULL);

        if (window == NULL) {
            glfwTerminate();
            return;
        }

        glfwMakeContextCurrent(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR);
        GL.createCapabilities();
        GL43.glViewport(0, 0, width, height);

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action != GLFW_RELEASE)
                    return;
                if (key == GLFW_KEY_ESCAPE)
                    glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetScrollCallback(window, getScrollCallback());
        glfwSetCursorPosCallback(window, getCursorPosCallback());

        int fbo = GL43.glGenFramebuffers();
        int textureIdentifier = GL43.glGenTextures();
        int rbo = GL43.glGenRenderbuffers();

        glfwSetFramebufferSizeCallback(
                window,
                getGlfwFramebufferSizeCallback(fbo, textureIdentifier, rbo)
        );


        setupFramebuffer(fbo, textureIdentifier, rbo);

        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0);

        int mandelVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(mandelVao);

        int mandelVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, mandelVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, SQUARE_VERTICES, GL43.GL_STATIC_DRAW);
        int mandelEbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ELEMENT_ARRAY_BUFFER, mandelEbo);
        GL43.glBufferData(GL43.GL_ELEMENT_ARRAY_BUFFER, SQUARE_INDICES, GL43.GL_STATIC_DRAW);

        int mandelVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER, "mandlebrot/shaders/vertex/mandelbrot.vert");
        int mandelFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                "mandlebrot/shaders/fragment/mandelbrot.frag");

        int mandelShaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(mandelShaderProgram, mandelVertexShader);
        GL43.glAttachShader(mandelShaderProgram, mandelFragmentShader);
        GL43.glLinkProgram(mandelShaderProgram);

        int[] linkStatus = new int[1];
        GL43.glGetProgramiv(mandelShaderProgram, GL43.GL_LINK_STATUS, linkStatus);
        if (linkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(mandelShaderProgram));
        }

        GL43.glUseProgram(mandelShaderProgram);

        int backgroundColorShaderLocation = GL43.glGetUniformLocation(mandelShaderProgram, "backgroundColor");
        int setColorShaderLocation = GL43.glGetUniformLocation(mandelShaderProgram, "setColor");

        Color backgroundColor = new Color(10, 3, 61, 255);
        float[] backgroundColorRGB = backgroundColor.getRGBColorComponents(new float[3]);

        Color setColor = new Color(69, 103, 179, 255);
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

        int coordinateInfoLocation = GL43.glGetUniformLocation(mandelShaderProgram, "coordInfo");
        GL43.glUniform4d(coordinateInfoLocation, coordOriginX, coordOriginY, coordXWidth, coordYWidth);

        int maxIterationsLocation = GL43.glGetUniformLocation(mandelShaderProgram, "maxIterations");
        GL43.glUniform1i(maxIterationsLocation, maxIterations);

        // linked, so we don't need anymore :)
        GL43.glDeleteShader(mandelVertexShader);
        GL43.glDeleteShader(mandelFragmentShader);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 3 * 4, 0);

        //Now set up the render stuff
        int renderVao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(renderVao);

        int renderVbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, renderVbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, QUAD_VERTICES, GL43.GL_STATIC_DRAW);

        int renderVertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER,
                "mandlebrot/shaders/vertex/render.vert");
        int renderFragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER,
                "mandlebrot/shaders/fragment/render.frag");

        int renderShaderProgram = GL43.glCreateProgram();
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
        glfwShowWindow(window);

        while (!glfwWindowShouldClose(window)) {
            GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, fbo);
            GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);
            GL43.glEnable(GL43.GL_DEPTH_TEST); //TODO - We don't actually need this!

            GL43.glUseProgram(mandelShaderProgram);
            GL43.glBindVertexArray(mandelVao);

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

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwTerminate();
    }

    private GLFWFramebufferSizeCallbackI getGlfwFramebufferSizeCallback(int fbo, int textureIdentifier, int rbo) {
        return (win, width, height) -> {
            this.width = width;
            this.height = height;
            GL43.glViewport(0, 0, this.width, this.height);

            setupFramebuffer(fbo, textureIdentifier, rbo);
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

            maxIterations = Math.max(50, (int)(50 * Math.pow(globalZoom, 0.1)));
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