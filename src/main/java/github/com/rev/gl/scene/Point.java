package github.com.rev.gl.scene;

import lombok.Getter;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Point {
    @Getter
    final Axes axes;
    @Getter
    final Position position;
    private final Matrix4f model;

    @Getter
    private final float[] positionFloats = new float[3];
    @Getter
    private final float[] modelFloats = new float[16];

    public Point(Axes axes, Position position) {
        this.axes = axes;
        this.position = position;
        this.model = modelFromAxis(axes);
        setPositionFloats();
        setModelFloats();
    }

    private static Matrix4f modelFromAxis(Axes axes) {
        return new Matrix4f(
                new Vector4f(axes.x, 0.0f),
                new Vector4f(axes.y, 0.0f),
                new Vector4f(axes.z, 0.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
        );
    }

    public final void move(final Vector3f amount) {
        position.xyz.add(amount);
        setPositionFloats();
    }

    private void setPositionFloats() {
        positionFloats[0] = position.xyz.x;
        positionFloats[1] = position.xyz.y;
        positionFloats[2] = position.xyz.z;
    }

    public final void rotate(final float angle, final Vector3f axis) {
        Matrix3f rotation3 = new Matrix3f().rotate(angle, axis);
        rotation3.transform(axes.x).normalize();
        rotation3.transform(axes.z).normalize();
        axes.x.cross(axes.z, axes.y);
        axes.z.cross(axes.y, axes.x);
        model.set(modelFromAxis(axes));
        setModelFloats();
    }

    private void setModelFloats() {
        model.get(modelFloats);
    }
}
