package github.com.rev.gl.uniform;

import org.lwjgl.opengl.GL43;

import java.util.function.BiConsumer;

public class UniformArray<T> extends Uniform {

    private final BiConsumer<T, Integer> elementSetter;
    private final T[] elements;

    public UniformArray(String name, boolean constant, BiConsumer<T, Integer> elementSetter, T[] elements) {
        super(name, constant);
        this.elementSetter = elementSetter;
        this.elements = elements;
    }

    @Override
    public void bindForReading(int shaderProgram) {
        for (int i = 0; i < elements.length; i++) {
            final int id = GL43.glGetUniformLocation(shaderProgram, String.format("%s[%s]", getName(), i));
            elementSetter.accept(elements[i], id);
        }
    }
}
