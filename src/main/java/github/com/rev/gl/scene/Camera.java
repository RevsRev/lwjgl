package github.com.rev.gl.scene;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {

    private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);

    float fov = (float) Math.PI / 4;

    final Vector3f cameraPosition = new Vector3f(0, 0, -1);
    final Vector3f direction = new Vector3f(0, 0, 1);
    final Vector3f right = direction.cross(WORLD_UP, new Vector3f()).normalize();
    final Vector3f up = right.cross(direction, new Vector3f());
    final Matrix4f view = new Matrix4f().lookAt(cameraPosition, cameraPosition.add(direction, new Vector3f()), WORLD_UP);
    final Matrix4f perspective = new Matrix4f().perspective(fov, 1.0f, 0.1f, 100.0f);


    public void move(final Vector3f amount) {
        cameraPosition.add(amount);
        view.identity().lookAt(cameraPosition, cameraPosition.add(direction, new Vector3f()), up);
    }

    public void incrementPitch(final float angle) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, right);
        rotation3.transform(up).normalize();
        rotation3.transform(direction).normalize();
        direction.cross(up, right).normalize();
        view.identity().lookAt(cameraPosition, cameraPosition.add(direction, new Vector3f()), up);
    }

    public void incrementYaw(final float angle) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, up);
        rotation3.transform(right).normalize();
        rotation3.transform(direction).normalize();
        right.cross(direction, up).normalize();

        view.identity().lookAt(cameraPosition, cameraPosition.add(direction, new Vector3f()), up);
    }

    public void rotate(final float angle, final Vector3f axis) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, axis);
        rotation3.transform(right).normalize();
        rotation3.transform(direction).normalize();
        right.cross(direction, up);
        direction.cross(up, right);

        view.identity().lookAt(cameraPosition, cameraPosition.add(direction, new Vector3f()), up);
    }
}
