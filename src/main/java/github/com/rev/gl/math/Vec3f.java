package github.com.rev.gl.math;

public class Vec3f {

    private static final int SIZE = 3;

    final float[] elements;

    public Vec3f(final float a0,
                 final float a1,
                 final float a2) {
        this(new float[]{a0, a1, a2});
    }

    Vec3f(float[] elements) {
        this.elements = elements;
    }

    public Vec3f mult(final float k) {
        return new Vec3f(
                k * elements[0],
                k * elements[1],
                k * elements[2]
        );
    }

    public float magnitude() {
        return Metrics.euclidian(elements);
    }

    public Vec3f normalise() {
        float magnitude = magnitude();
        return new Vec3f(
                elements[0] / magnitude,
                elements[1] / magnitude,
                elements[2] / magnitude
        );
    }

    public Vec3f add(final Vec3f other) {
        return new Vec3f(
                elements[0] + other.elements[0],
                elements[1] + other.elements[1],
                elements[2] + other.elements[2]
        );
    }

    public Vec3f minus(final Vec3f other) {
        return new Vec3f(
                elements[0] - other.elements[0],
                elements[1] - other.elements[1],
                elements[2] - other.elements[2]
        );
    }

    public Vec3f cross(final Vec3f other) {
        return new Vec3f(
                elements[1] * other.elements[2] - elements[2] * other.elements[1],
                - elements[0] * other.elements[2] + elements[2] * other.elements[0],
                elements[0] * other.elements[1] - elements[1] * other.elements[0]
        );
    }
}
