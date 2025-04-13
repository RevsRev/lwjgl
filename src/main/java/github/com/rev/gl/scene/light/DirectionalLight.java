package github.com.rev.gl.scene.light;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.function.BiConsumer;

public class DirectionalLight implements Light{
    private final Vector3f ambient = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f direction = new Vector3f(0.0f, -1.0f, 0.0f);

    public static Map<String, BiConsumer<Integer, DirectionalLight>> structUniforms() {
        return Map.of(
                "direction", (id, dL) -> GL43.glUniform3f(id, dL.direction.x, dL.direction.y, dL.direction.z),
                "ambient", (id, dL) -> GL43.glUniform3f(id, dL.ambient.x, dL.ambient.y, dL.ambient.z),
                "diffuse", (id, dL) -> GL43.glUniform3f(id, dL.diffuse.x, dL.diffuse.y, dL.diffuse.z),
                "specular", (id, dL) -> GL43.glUniform3f(id, dL.specular.x, dL.specular.y, dL.specular.z)
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
