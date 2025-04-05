package github.com.rev.gl.scene;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Point {

    private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);

    float fov = (float) Math.PI / 4;

    final Vector3f position = new Vector3f(0, 0, -1);
    final Vector3f direction = new Vector3f(0, 0, 1);
    final Vector3f right = direction.cross(WORLD_UP, new Vector3f()).normalize();
    final Vector3f up = right.cross(direction, new Vector3f());


    public void move(final Vector3f amount) {
        position.add(amount);
    }

    public void rotate(final float angle, final Vector3f axis) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, axis);
        rotation3.transform(right).normalize();
        rotation3.transform(direction).normalize();
        right.cross(direction, up);
        direction.cross(up, right);
    }
}
