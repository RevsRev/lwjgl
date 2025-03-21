package github.com.rev;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main
{
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final float[] TRIANGLE_VERTICES = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f
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

    public static void main(String[] args)
    {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        long window = glfwCreateWindow(WIDTH, HEIGHT, "Mandelbrot", NULL, NULL);

        if (window == NULL) {
            glfwTerminate();
            return;
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GL43.glViewport(0, 0, 800, 600);

        glfwSetFramebufferSizeCallback(
                window,
                (win, width, height) -> GL43.glViewport(0, 0, width, height)
        );

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action != GLFW_RELEASE)
                    return;
                if (key == GLFW_KEY_ESCAPE)
                    glfwSetWindowShouldClose(window, true);
            }
        });

        int vao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(vao);

        //Draw a triangle
//        int vbo = GL43.glGenBuffers();
//        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo);
//        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, TRIANGLE_VERTICES, GL43.GL_STATIC_DRAW);

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

            // Drawing triangle demo
//            GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 3);

            // Drawing square
            GL43.glDrawElements(GL43.GL_TRIANGLES, 6, GL43.GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwTerminate();
    }

    private static CharSequence loadShader(String resourcePath) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(resourcePath)) {
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