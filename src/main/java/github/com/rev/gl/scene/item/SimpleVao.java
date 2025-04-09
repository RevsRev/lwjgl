package github.com.rev.gl.scene.item;

import org.lwjgl.opengl.GL43;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class SimpleVao {

    private final int vao;
    private final int vbo;

    private SimpleVao(int vao, int vbo) {
        this.vao = vao;
        this.vbo = vbo;
    }

    public static SimpleVao create(final float[] vertices) {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = GL43.glGenBuffers();

        //CUBE
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, vertices, GL43.GL_STATIC_DRAW);

        GL43.glEnableVertexAttribArray(0);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 8 * 4, 0);
        GL43.glEnableVertexAttribArray(1);
        GL43.glVertexAttribPointer(1, 3, GL43.GL_FLOAT, false, 8 * 4, 3 * 4);
        GL43.glEnableVertexAttribArray(2);
        GL43.glVertexAttribPointer(2, 2, GL43.GL_FLOAT, false, 8 * 4, 6 * 4);

        return new SimpleVao(vao, vbo);
    }

    public void draw() {
        GL43.glBindVertexArray(vao);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 36);
    }
}
