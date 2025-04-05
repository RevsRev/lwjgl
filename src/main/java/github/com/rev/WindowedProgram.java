package github.com.rev;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;

public abstract class WindowedProgram implements Runnable{
    public int width = 800;
    public int height = 600;
    public final String title;

    public WindowedProgram(String title) {
        this.title = title;
    }

    public abstract void setupCallbacks(long window);

    public abstract void init();

    public int getCursorMode() {
        return GLFW_CURSOR;
    }
}
