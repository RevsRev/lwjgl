package github.com.rev.gl.scene;

import github.com.rev.gl.math.Mat4f;
import github.com.rev.gl.math.Transform;
import github.com.rev.gl.math.Vec3f;

public final class Camera {

    private static final Vec3f WORLD_UP = new Vec3f(0, 1, 0);

    final Vec3f position = new Vec3f(0, 0, 0);
    final Vec3f target = new Vec3f(1, 0, 0);
    final Vec3f direction = position.minus(position).normalise();
    final Vec3f right = WORLD_UP.cross(direction).normalise();
    final Vec3f up = direction.cross(right).normalise();
    final Mat4f view = Transform.lookAt(position, target, right);
}
