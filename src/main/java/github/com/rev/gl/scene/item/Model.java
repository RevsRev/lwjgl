package github.com.rev.gl.scene.item;

import github.com.rev.gl.shader.Uniforms;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private final List<Mesh> meshes = new ArrayList<>();

    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
    }

    public void render(Uniforms sceneUniforms) {
        meshes.forEach(m -> m.render(sceneUniforms));
    }

}
