package github.com.rev.gl.scene;

import org.joml.Matrix3f;
import org.joml.Vector3f;

public final class Axes {

    private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);

    final Vector3f z = new Vector3f(0, 0, 1);
    final Vector3f x = z.cross(WORLD_UP, new Vector3f()).normalize();
    final Vector3f y = x.cross(z, new Vector3f());

    public void rotate(final float angle, final Vector3f axis) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, axis);
        rotation3.transform(x).normalize();
        rotation3.transform(z).normalize();
        x.cross(z, y);
        z.cross(y, x);
    }
}
