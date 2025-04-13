package github.com.rev.gl.scene.light;

import org.joml.Vector3f;

public interface Light {
    void setAmbient(Vector3f ambient);
    void setDiffuse(Vector3f diffuse);
    void setSpecular(Vector3f ambient);

    Vector3f getAmbient();
    Vector3f getDiffuse();
    Vector3f getSpecular();

}
