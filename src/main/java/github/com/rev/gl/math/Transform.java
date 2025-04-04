package github.com.rev.gl.math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public final class Transform {

    private Transform() {
    }

    public static Mat4f lookAt(final Vec3f position, final Vec3f target, final Vec3f right) {
        final Vec3f direction = target.minus(position).normalise();
        final Vec3f up = right.cross(direction).normalise();
        final Mat4f transform = new Mat4f(
                right.elements[0], right.elements[1], right.elements[2], 0,
                up.elements[0], up.elements[1], up.elements[2], 0,
                direction.elements[0], direction.elements[1], direction.elements[2], 0,
                0,0,0,0

        );
        Mat4f posMat4 = new Mat4f(
                1, 0, 0, -position.elements[0],
                0, 1, 0, -position.elements[1],
                0, 0, 1, -position.elements[2],
                0, 0, 0, 1
        );
        return transform.mult(posMat4);
    }

    public static Mat4f translate(final Vec3f delta) {
        return new Mat4f(
                1, 0, 0, delta.elements[0],
                0, 1, 0, delta.elements[1],
                0, 0, 1, delta.elements[2],
                0, 0, 0, 1
        );
    }

    public static Mat4f rotate(final float theta, final Vec3f axis) {
        float c = (float) cos(theta);
        float cM1 = 1 - c;
        float s = (float) sin(theta);

        float x = axis.elements[0];
        float y = axis.elements[1];
        float z = axis.elements[2];
        return new Mat4f(
                x * x * cM1 + c,        x * y * cM1 - z * s,    x * z * cM1 + y * s,    1,
                x * y * cM1 + z * s,    y * y * cM1 + c,        y * z * cM1 - x * s,    1,
                x * z * cM1 - y * s,    y * z * cM1 + x * s,    z * z * cM1 + c,        1,
                0,                      0,                      0,                      1
        );
    }

}
