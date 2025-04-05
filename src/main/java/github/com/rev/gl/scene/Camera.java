package github.com.rev.gl.scene;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {

    float fov = (float) Math.PI / 4;

    final Axes axes = new Axes();
    private final Position position = new Position();

    final Matrix4f view = new Matrix4f().lookAt(position.xyz, position.xyz.add(axes.z, new Vector3f()), axes.y);
    final Matrix4f perspective = new Matrix4f().perspective(fov, 1.0f, 0.1f, 100.0f);


    public void move(final Vector3f amount) {
        position.xyz.add(amount);
        view.identity().lookAt(position.xyz, position.xyz.add(axes.z, new Vector3f()), axes.y);
    }

    public void rotate(final float angle, final Vector3f axis) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, axis);
        rotation3.transform(axes.x).normalize();
        rotation3.transform(axes.z).normalize();
        axes.x.cross(axes.z, axes.y);
        axes.z.cross(axes.y, axes.x);

        view.identity().lookAt(position.xyz, position.xyz.add(axes.z, new Vector3f()), axes.y);
    }
}
