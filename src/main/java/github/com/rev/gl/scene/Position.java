package github.com.rev.gl.scene;

import org.joml.Vector3f;

public final class Position {

    public final Vector3f xyz = new Vector3f(0, 0, -1);
    public void move(final Vector3f amount) {
        xyz.add(amount);
    }
}
