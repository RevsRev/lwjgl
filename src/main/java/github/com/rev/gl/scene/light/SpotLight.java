package github.com.rev.gl.scene.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class SpotLight {
    private Vector3f position;
    private float cutoff;

    // TODO - Not sure if we want these in this model?
    private float constant;
    private float linear;
    private float quadratic;

    //TODO - Smoothing the cutoff?
}
