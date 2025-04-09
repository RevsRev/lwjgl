package github.com.rev.gl.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {

    float fov = (float) Math.PI / 4;


    final Point p = new Point(new Axes(), new Position());
    private final Matrix4f view = new Matrix4f().lookAt(p.position.xyz, p.position.xyz.add(p.axes.z, new Vector3f()), p.axes.y);
    private final Matrix4f perspective = new Matrix4f().perspective(fov, 1.0f, 0.1f, 100.0f);

    @Getter
    private final float[] viewFloats = new float[16];
    @Getter
    private final float[] perspectiveFloats = new float[16];

    public Camera() {
        view.get(viewFloats);
        perspective.get(perspectiveFloats);
    }

    public void move(final Vector3f amount) {
        p.move(amount);
        view.identity().lookAt(p.position.xyz, p.position.xyz.add(p.axes.z, new Vector3f()), p.axes.y);
        view.get(viewFloats);
    }

    public void rotate(final float angle, final Vector3f axis) {
        p.rotate(angle, axis);
        view.identity().lookAt(p.position.xyz, p.position.xyz.add(p.axes.z, new Vector3f()), p.axes.y);
        view.get(viewFloats);
    }
}
