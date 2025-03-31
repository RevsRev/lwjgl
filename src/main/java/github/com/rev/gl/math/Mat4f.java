package github.com.rev.gl.math;

public final class Mat4f {

    private static final int SIZE = 4;
    final float[][] elements;

    public Mat4f(float a00, float a01, float a02, float a03,
                 float a10, float a11, float a12, float a13,
                 float a20, float a21, float a22, float a23,
                 float a30, float a31, float a32, float a33
    ) {
        this(new float[][]{
                {a00, a01, a02, a03},
                {a10, a11, a12, a13},
                {a20, a21, a22, a23},
                {a30, a31, a32, a33}
        });
    }

    Mat4f(float[][] elements) {
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

    public Mat4f mult(Mat4f other) {
        float[][] result = new float[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                result[i][j] = 0;
                for (int k = 0; k < SIZE; k ++) {
                    result[i][j] += elements[i][k] * other.elements[k][j];
                }
            }
        }
        return new Mat4f(result);
    }

}
