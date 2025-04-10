package github.com.rev.gl.scene.light;

import org.joml.Vector3f;

public interface Light {
    Vector3f ambient = new Vector3f(0.1f, 0.1f, 0.1f);
    Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);
}
