package github.com.rev;

public abstract class WindowedProgram implements Runnable{
    public int width = 800;
    public int height = 600;
    public final String title;

    public WindowedProgram(String title) {
        this.title = title;
    }

    public abstract void setupCallbacks(long window);

    public abstract void init();
}
