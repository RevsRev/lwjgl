package github.com.rev.gl.uniform;

import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.Consumer;

public class UniformStruct extends Uniform {

    final Map<String, Consumer<Integer>> structSetters;

    public UniformStruct(final String name, final boolean constant, Map<String, Consumer<Integer>> structSetters) {
        super(name, constant);
        this.structSetters = structSetters;
    }

    @Override
    public void bind(int shaderProgram) {
        structSetters.forEach(
                (structPropertyName, structSetter) -> {
                    final int id = GL43.glGetUniformLocation(shaderProgram, String.format("%s.%s", getName(), structPropertyName));
                    structSetter.accept(id);
                }
        );
    }
}
