package github.com.rev.gl.scene.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public abstract class Light {
    Vector3f ambient = new Vector3f(0.1f, 0.1f, 0.1f);
    Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);
}
