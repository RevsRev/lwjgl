package github.com.rev;

import org.lwjgl.opengl.GL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GlfwManager {

    private final Map<WindowedProgram, Long> programs = new HashMap<>();

    private GlfwManager() {
    }

    //TODO - Turn this into a singleton?
    public static GlfwManager instance() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        return new GlfwManager();
    }

    public void addWindowedProgram(WindowedProgram program) {
        long window = glfwCreateWindow(program.width, program.height, program.title, NULL, NULL);

        if (window == NULL) {
            glfwTerminate();
        }

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR);
        program.setupCallbacks(window);
        programs.put(program, window);
    }

    public void run() {
        CountDownLatch latch = new CountDownLatch(programs.size());
        for (Map.Entry<WindowedProgram, Long> programAndWindow : programs.entrySet()) {
            final long window = programAndWindow.getValue();
            final WindowedProgram program = programAndWindow.getKey();
            Thread t = new Thread(() -> {

//                glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
//                glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
//                glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
//                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

                try {
                   glfwMakeContextCurrent(window);
                   program.init();
                    glfwShowWindow(window);
                   while (!glfwWindowShouldClose(window)) {
                       program.run();
                       glfwSwapBuffers(window);
                   }
               } finally {
                   latch.countDown();
               }
            });
            t.start();
        }

        try {
            while (latch.getCount() != 0) {
                glfwPollEvents();
            }
        } finally {
            glfwTerminate();
        }
    }

}
