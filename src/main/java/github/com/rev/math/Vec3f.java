package github.com.rev.math;

public class Vec3f {

    final float[] elements;

    public Vec3f(float a0, float a1, float a2) {
        this(new float[]{a0, a1, a2});
    }

    Vec3f(float[] elements) {
        this.elements = elements;
    }

    public Vec3f mult(float k) {
        return new Vec3f(k * elements[0], k * elements[1], k * elements[2]);
    }

    public float magnitude() {
        return Metrics.euclidian(elements);
    }
}
