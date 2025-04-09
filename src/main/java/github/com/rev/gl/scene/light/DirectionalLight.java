package github.com.rev.gl.scene.light;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.BiConsumer;

public class DirectionalLight extends Light{
    private Vector3f direction = new Vector3f(0.0f, -1.0f, 0.0f);

    public static Map<String, BiConsumer<Integer, DirectionalLight>> structUniforms() {
        return Map.of(
                "direction", (id, dL) -> GL43.glUniform3f(id, dL.direction.x, dL.direction.y, dL.direction.z),
                "ambient", (id, dL) -> GL43.glUniform3f(id, dL.ambient.x, dL.ambient.y, dL.ambient.z),
                "diffuse", (id, dL) -> GL43.glUniform3f(id, dL.diffuse.x, dL.diffuse.y, dL.diffuse.z),
                "specular", (id, dL) -> GL43.glUniform3f(id, dL.specular.x, dL.specular.y, dL.specular.z)
        );
    }
}
