package github.com.rev.math;

public final class Mat3f {

    private static final int SIZE = 3;
    final float[][] elements;

    public Mat3f(float a00, float a01, float a02,
                 float a10, float a11, float a12,
                 float a20, float a21, float a22) {
        this(new float[][]{
                {a00, a01, a02},
                {a10, a11, a12},
                {a20, a21, a22}
        });
    }

    Mat3f(float[][] elements) {
        this.elements = elements;
    }

    public Vec3f mult(Vec3f a) {
        float[] result = new float[3];
        for (int i = 0; i < elements.length; i++) {
            result[i] = 0;
            for (int j = 0; j < elements[i].length; j++) {
                result[i] +=  elements[i][j] * a.elements[j];
            }
        }
        return new Vec3f(result);
    }

    public Mat3f mult(Mat3f other) {
        float[][] result = new float[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                result[i][j] = 0;
                for (int k = 0; k < SIZE; k ++) {
                    result[i][j] += elements[i][k] * other.elements[k][j];
                }
            }
        }
        return new Mat3f(result);
    }

}
