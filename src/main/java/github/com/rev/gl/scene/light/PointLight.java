package github.com.rev.gl.scene.light;

import github.com.rev.gl.scene.Axes;
import github.com.rev.gl.scene.Point;
import github.com.rev.gl.scene.Position;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.BiConsumer;

public class PointLight extends Point implements Light {

    private final Vector3f ambient = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);

    @Getter @Setter
    private float constant = 1.0f;
    @Getter @Setter
    private float linear = 1.0f;
    @Getter @Setter
    private float quadratic = 1.0f;

    public PointLight(Axes axes, Position position) {
        super(axes, position);
    }

    public static Map<String, BiConsumer<Integer, PointLight>> structUniforms() {
        return Map.of(
                "position", (id, pL) -> GL43.glUniform3f(id, pL.getPosition().xyz.x, pL.getPosition().xyz.y, pL.getPosition().xyz.z),
                "ambient", (id, pL) -> GL43.glUniform3f(id, pL.ambient.x, pL.ambient.y, pL.ambient.z),
                "diffuse", (id, pL) -> GL43.glUniform3f(id, pL.diffuse.x, pL.diffuse.y, pL.diffuse.z),
                "specular", (id, pL) -> GL43.glUniform3f(id, pL.specular.x, pL.specular.y, pL.specular.z),
                "constant", (id, pL) -> GL43.glUniform1f(id, pL.constant),
                "linear", (id, pL) -> GL43.glUniform1f(id, pL.linear),
                "quadratic", (id, pL) -> GL43.glUniform1f(id, pL.quadratic)
        );
    }


    @Override
    public void setAmbient(Vector3f ambient) {
        this.ambient.set(ambient);
    }

    @Override
    public void setDiffuse(Vector3f diffuse) {
        this.diffuse.set(diffuse);
    }

    @Override
    public void setSpecular(Vector3f ambient) {
        this.specular.set(specular);
    }

    @Override
    public Vector3f getAmbient() {
        return new Vector3f(ambient);
    }

    @Override
    public Vector3f getDiffuse() {
        return new Vector3f(ambient);
    }

    @Override
    public Vector3f getSpecular() {
        return new Vector3f(ambient);
    }
}
