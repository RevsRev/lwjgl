package github.com.rev.gl.uniform;

import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UniformStructArray<T> extends Uniform {

    final Map<String, BiConsumer<T, Integer>> structSetters;
    private final T[] elements;

    public UniformStructArray(String name, boolean constant, Map<String, BiConsumer<T, Integer>> structSetters, T[] elements) {
        super(name, constant);
        this.structSetters = structSetters;
        this.elements = elements;
    }

    @Override
    public void bind(int shaderProgram) {
        for (int i = 0; i < elements.length; i++) {
            int finalI = i;
            structSetters.forEach(
                    (structPropertyName, structSetter) -> {
                        final int id = GL43.glGetUniformLocation(shaderProgram, String.format("%s[%s].%s", getName(),
                                finalI, structPropertyName));
                        structSetter.accept(elements[finalI], id);
                    }
            );
        }
    }
}
