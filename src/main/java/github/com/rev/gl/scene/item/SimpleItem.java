package github.com.rev.gl.scene.item;

import github.com.rev.gl.scene.Axes;
import github.com.rev.gl.scene.Point;
import github.com.rev.gl.scene.Position;
import github.com.rev.gl.shader.ShaderProgram;
import github.com.rev.gl.shader.Uniforms;
import github.com.rev.gl.texture.LayerManager;
import github.com.rev.gl.texture.image.ImageTexture;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.List;

public class SimpleItem {

    private final SimpleVao vao;
    private final ShaderProgram shaderProgram;
    private final Uniforms frameUniforms;
    public final Point point;

    public SimpleItem(final SimpleVao vao,
                      final ShaderProgram shaderProgram,
                      final Uniforms frameUniforms,
                      final Point point) {
        this.vao = vao;
        this.shaderProgram = shaderProgram;
        this.frameUniforms = frameUniforms;
        this.point = point;
    }


    public void render(final Uniforms sceneUniforms) {
        shaderProgram.use(sceneUniforms, frameUniforms);
        vao.draw();
    }

    public static final class Builder {
        private final float[] vertices;
        private final String vertexShader;
        private final String fragmentShader;
        private Point point = new Point(new Axes(), new Position());

        private final List<String> ambientTextures = new ArrayList<>();
        private final List<String> diffuseTextures = new ArrayList<>();
        private final List<String> specularTextures = new ArrayList<>();
        private float shininess = 1.0f;

        public Builder(float[] vertices, String vertexShader, String fragmentShader) {
            this.vertices = vertices;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        public SimpleItem build(final LayerManager layerManager) {

            Uniforms constantUniforms = new Uniforms();

            constantUniforms.add(addMaterials(ambientTextures, "ambientTextures", layerManager));
            constantUniforms.add(addMaterials(diffuseTextures, "diffuseTextures", layerManager));
            constantUniforms.add(addMaterials(specularTextures, "specularTextures", layerManager));
            constantUniforms.addPrimitiveUniform("shininess", shininess, GL43::glUniform1f);

            final Uniforms frameUniforms = new Uniforms();
            frameUniforms.addPrimitiveUniform("position", point, (id, pt) -> GL43.glUniform3fv(id, pt.getPositionFloats()));
            frameUniforms.addPrimitiveUniform("model", point, (id, pt) -> GL43.glUniformMatrix4fv(id, false, pt.getModelFloats()));
            SimpleVao vao = SimpleVao.create(vertices);

            ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
            shaderProgram.init();
            shaderProgram.setConstantUniforms(constantUniforms);

            return new SimpleItem(vao, shaderProgram, frameUniforms, point);
        }

        private Uniforms addMaterials(final List<String> textures, final String uniformName, final LayerManager layerManager) {

            final Uniforms uniforms = new Uniforms();
            if (textures.isEmpty()) {
                return uniforms;
            }

            final ImageTexture[] imageTextures = new ImageTexture[textures.size()];
            for (int i = 0; i < textures.size(); i++) {
                ImageTexture imageTexture = ImageTexture.fromFile(textures.get(i), 512, 512, layerManager.next());
                imageTexture.init();
                imageTextures[i] = imageTexture;
            }
            uniforms.addArrayUniform(uniformName, imageTextures, (id, tex) -> tex.bindForReading(id));
            uniforms.addPrimitiveUniform(uniformName + "Size", textures.size(), GL43::glUniform1i);
            return uniforms;
        }

        public Builder setPoint(final Point point) {
            this.point = point;
            return this;
        }

        public Builder addAmbientTexture(final String texture) {
            ambientTextures.add(texture);
            return this;
        }

        public Builder addDiffuseTexture(final String texture) {
            diffuseTextures.add(texture);
            return this;
        }

        public Builder addSpecularTexture(final String texture) {
            specularTextures.add(texture);
            return this;
        }

        public Builder setShininess(final float shininess) {
            this.shininess = shininess;
            return this;
        }
    }
}
