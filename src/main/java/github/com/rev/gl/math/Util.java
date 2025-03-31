package github.com.rev.gl.math;

public final class Util {

    private Util() {
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

}
