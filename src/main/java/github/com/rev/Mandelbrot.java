package github.com.rev;

import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        glfwSetFramebufferSizeCallback(
                window,
                getGlfwFramebufferSizeCallback()
        );

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

        int vao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(vao);

        //Draw a square
        int vbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, SQUARE_VERTICES, GL43.GL_STATIC_DRAW);
        int ebo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL43.glBufferData(GL43.GL_ELEMENT_ARRAY_BUFFER, SQUARE_INDICES, GL43.GL_STATIC_DRAW);

        int vertexShader = GL43.glCreateShader(GL43.GL_VERTEX_SHADER);
        GL43.glShaderSource(vertexShader, loadShader("shaders/vertex/vertex.vert"));
        GL43.glCompileShader(vertexShader);

        int[] vertCompileStatus = new int[1];
        GL43.glGetShaderiv(vertexShader, GL43.GL_COMPILE_STATUS, vertCompileStatus);

        if (vertCompileStatus[0] != 1) {
            System.out.print(GL43.glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = GL43.glCreateShader(GL43.GL_FRAGMENT_SHADER);
        GL43.glShaderSource(fragmentShader, loadShader("shaders/fragment/frag.frag"));
        GL43.glCompileShader(fragmentShader);

        int[] fragCompileStatus = new int[1];
        GL43.glGetShaderiv(fragmentShader, GL43.GL_COMPILE_STATUS, fragCompileStatus);

        if (fragCompileStatus[0] != 1) {
            String info = GL43.glGetShaderInfoLog(fragmentShader);
            System.out.print(info);
        }

        int shaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(shaderProgram, vertexShader);
        GL43.glAttachShader(shaderProgram, fragmentShader);
        GL43.glLinkProgram(shaderProgram);

        int[] linkStatus = new int[1];
        GL43.glGetProgramiv(shaderProgram, GL43.GL_LINK_STATUS, linkStatus);
        if (linkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(shaderProgram));
        }

        GL43.glUseProgram(shaderProgram);

        int backgroundColorShaderLocation = GL43.glGetUniformLocation(shaderProgram, "backgroundColor");
        int setColorShaderLocation = GL43.glGetUniformLocation(shaderProgram, "setColor");

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

        int coordinateInfoLocation = GL43.glGetUniformLocation(shaderProgram, "coordInfo");
        GL43.glUniform4d(coordinateInfoLocation, coordOriginX, coordOriginY, coordXWidth, coordYWidth);

        int maxIterationsLocation = GL43.glGetUniformLocation(shaderProgram, "maxIterations");
        GL43.glUniform1i(maxIterationsLocation, maxIterations);

        // linked, so we don't need anymore :)
        GL43.glDeleteShader(vertexShader);
        GL43.glDeleteShader(fragmentShader);

        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 3 * 4, 0);
        GL43.glEnableVertexAttribArray(0);

        glfwSwapInterval(1);
        glfwShowWindow(window);

        while (!glfwWindowShouldClose(window)) {
            GL43.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

            GL43.glUniform4d(coordinateInfoLocation, coordOriginX, coordOriginY, coordXWidth, coordYWidth);
            GL43.glUniform1i(maxIterationsLocation, maxIterations);

            GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwTerminate();
    }

    private GLFWFramebufferSizeCallbackI getGlfwFramebufferSizeCallback() {
        return (win, width, height) -> {
            this.width = width;
            this.height = height;
            GL43.glViewport(0, 0, this.width, this.height);
        };
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

    private CharSequence loadShader(String resourcePath) {
        try (InputStream is = Mandelbrot.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException(String.format("Failed to load shader '%s'", resourcePath));
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = is.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to load shader '%s'", resourcePath), e);
        }
    }

}