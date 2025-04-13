package github.com.rev.gl.scene.item;

import org.lwjgl.opengl.GL43;

import java.util.Optional;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class SimpleVao {

    private final int vao;
    private final Runnable renderFunc;

    private SimpleVao(int vao, Runnable renderFunc) {
        this.vao = vao;
        this.renderFunc = renderFunc;
    }

    public static SimpleVao create(final float[] vertices, Optional<int[]> indices) {
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

        if (indices.isPresent()) {
            int[] indicesArr = indices.get();
            int ebo = GL43.glGenBuffers();
            GL43.glBindBuffer(GL43.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL43.glBufferData(GL43.GL_ELEMENT_ARRAY_BUFFER, indicesArr, GL43.GL_STATIC_DRAW);
        }

        final Runnable renderFunc = indices.isPresent() ? () -> {
            GL43.glDrawElements(GL43.GL_TRIANGLES, indices.get().length, GL43.GL_UNSIGNED_INT, 0);
        } : () -> {
            GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices.length / 8);
        };

        return new SimpleVao(vao, renderFunc);
    }

    public void draw() {
        GL43.glBindVertexArray(vao);
        renderFunc.run();
    }
}
