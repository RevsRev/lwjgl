package github.com.rev.gl.scene.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.BiConsumer;

@Getter
@Setter
public class PointLight extends Light {
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private float constant = 1.0f;
    private float linear = 1.0f;
    private float quadratic = 1.0f;

    public static Map<String, BiConsumer<Integer, PointLight>> structUniforms() {
        return Map.of(
                "position", (id, pL) -> GL43.glUniform3f(id, pL.position.x, pL.position.y, pL.position.z),
                "ambient", (id, pL) -> GL43.glUniform3f(id, pL.ambient.x, pL.ambient.y, pL.ambient.z),
                "diffuse", (id, pL) -> GL43.glUniform3f(id, pL.diffuse.x, pL.diffuse.y, pL.diffuse.z),
                "specular", (id, pL) -> GL43.glUniform3f(id, pL.specular.x, pL.specular.y, pL.specular.z),
                "constant", (id, pL) -> GL43.glUniform1f(id, pL.constant),
                "linear", (id, pL) -> GL43.glUniform1f(id, pL.linear),
                "quadratic", (id, pL) -> GL43.glUniform1f(id, pL.quadratic)
        );
    }
}
