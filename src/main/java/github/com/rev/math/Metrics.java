package github.com.rev.math;

public final class Metrics {

    private Metrics() {
    }

    public static float euclidian(float[] vector) {
        float sqSum = 0.0f;
        for (float a : vector) {
            sqSum += a * a;
        }
        return (float)Math.sqrt(sqSum);
    }

}
