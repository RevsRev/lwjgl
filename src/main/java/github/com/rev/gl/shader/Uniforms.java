package github.com.rev.gl.shader;

import org.lwjgl.opengl.GL43;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Uniforms {

    private final Map<String, Consumer<Integer>> uniforms = new HashMap<String, Consumer<Integer>>();

    public Uniforms() {
    }

    public final void add(final Uniforms other) {
        uniforms.putAll(other.uniforms);
    }

    public <T> void addPrimitiveUniform(final String name, final T value, final BiConsumer<Integer, T> setter) {
        uniforms.put(name, id -> setter.accept(id, value));
    }

    public <T> void addArrayUniform(final String name, final T[] values, final BiConsumer<Integer, T> elementSetter) {
        for (int i = 0; i < values.length; i++) {
            addPrimitiveUniform(String.format("%s[%s]", name, i), values[i], elementSetter);
        }
    }

    public <T> void addStructUniform(final String name, final T value,
                                     final Map<String, BiConsumer<Integer, T>> properties) {
        properties.forEach((k, v) ->
                addPrimitiveUniform(String.format("%s.%s", name, k), value, v)
        );
    }

    public <T> void addStructArrayUniform(final String name, final T[] values,
                                          final Map<String, BiConsumer<Integer, T>> properties) {
        for (int i = 0; i < values.length; i++) {
            addStructUniform(String.format("%s[%s]", name, i), values[i], properties);
        }
    }

    public void loadUniforms(final int shaderProgramId) {
        uniforms.forEach(
                (uniformName, v) -> {
                    v.accept(GL43.glGetUniformLocation(shaderProgramId, uniformName));
                });
    }
}