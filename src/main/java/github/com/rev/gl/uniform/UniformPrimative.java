package github.com.rev.gl.uniform;

import org.lwjgl.opengl.GL43;

import java.util.function.Consumer;

public class UniformPrimative extends Uniform {

    private final Consumer<Integer> setter;

    public UniformPrimative(String name, boolean constant, Consumer<Integer> setter) {
        super(name, constant);
        this.setter = setter;
    }

    @Override
    public void bindForReading(int shaderProgram) {
        final int id = GL43.glGetUniformLocation(shaderProgram, getName());
        setter.accept(id);
    }
}
