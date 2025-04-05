package github.com.rev.gl.scene;

import github.com.rev.gl.math.Precision;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

public final class CameraController {

    @Getter @Setter
    private double speed = 5.0d;
    private float sensitivity = 0.1f;
    private final Camera camera;

    private final Map<Integer, Integer> keyStates = new HashMap<>(Map.of(
            GLFW_KEY_W, GLFW_RELEASE,
            GLFW_KEY_A, GLFW_RELEASE,
            GLFW_KEY_S, GLFW_RELEASE,
            GLFW_KEY_D, GLFW_RELEASE,
            GLFW_KEY_SPACE, GLFW_RELEASE,
            GLFW_KEY_LEFT_SHIFT, GLFW_RELEASE
    ));

    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private Vector3f axis = new Vector3f();
    private float angle = 0.0f;

    public CameraController(Camera camera) {
        this.camera = camera;
    }

    public void handleKey(int key, int scancode, int action, int mods) {
        if (!keyStates.containsKey(key)) {
            return;
        }

        if (action == GLFW_REPEAT) {
            return;
        }

        keyStates.put(key, action);
    }

    public void handleMouse(double xpos, double ypos) {

        float delX = (float) (lastMouseX - xpos);
        float delY = (float) (lastMouseY - ypos);

        lastMouseX = xpos;
        lastMouseY = ypos;

        axis = new Matrix3f(camera.right.mul(-1, new Vector3f()), camera.up, camera.direction).transform(new Vector3f(-delY, delX, 0.0f));
        angle = axis.length();

        if (angle < Precision.EPSILON) {
            axis = new Vector3f();
            angle = 0.0f;
            return;
        }

        axis.normalize();
    }

    public void process(double frameTime) {
        processMovement(frameTime);
        processDirection(frameTime);
    }

    private void processMovement(double frameTime) {
        Vector3f direction = new Vector3f();
        keyStates.forEach((key, state) ->
        {
            if (state == GLFW_PRESS) {

                switch (key) {
                    case GLFW_KEY_D -> direction.add(camera.right);
                    case GLFW_KEY_A -> direction.add(camera.right.mul(-1, new Vector3f()));
                    case GLFW_KEY_W -> direction.add(camera.direction);
                    case GLFW_KEY_S -> direction.add(camera.direction.mul(-1, new Vector3f()));
                    case GLFW_KEY_SPACE -> direction.add(camera.up);
                    case GLFW_KEY_LEFT_SHIFT -> direction.add(camera.up.mul(-1, new Vector3f()));
                }
            }
        });

        if (direction.equals(new Vector3f(), Precision.EPSILON)) {
            return;
        }

        direction.normalize();
        float magnitude = (float)(frameTime * speed);
        direction.mul(magnitude);
        camera.move(direction);
    }

    private void processDirection(double frameTime) {

        if (angle < Precision.EPSILON) {
            return;
        }

        float trueAngle = (float) frameTime * angle * sensitivity;

        camera.rotate(trueAngle, axis);

        angle = 0.0f;
    }
}
