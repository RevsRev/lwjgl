package github.com.rev.gl.scene.item;

import github.com.rev.gl.scene.Axes;
import github.com.rev.gl.scene.Point;
import github.com.rev.gl.scene.Position;
import github.com.rev.gl.shader.ShaderProgram;
import github.com.rev.gl.shader.Uniforms;
import github.com.rev.gl.texture.LayerManager;
import github.com.rev.gl.texture.image.ImageTexture;
import org.lwjgl.opengl.GL43;

import java.util.Optional;

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
        private Optional<String> texture = Optional.empty();
        private Point point = new Point(new Axes(), new Position());
        private Optional<Material.Builder> material = Optional.empty();

        public Builder(float[] vertices, String vertexShader, String fragmentShader) {
            this.vertices = vertices;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        public SimpleItem build(final LayerManager layerManager) {

            Uniforms constantUniforms = new Uniforms();

            if (texture.isPresent()) {
                final ImageTexture imageTexture = ImageTexture.fromFile(texture.get(), 512, 512, layerManager.next()); //TODO - Extract to parameters!
                imageTexture.init();

                constantUniforms.addPrimitiveUniform("aTexture", imageTexture, (id, tex) -> {
                    imageTexture.bindForReading(id);
                });
            }

            material.ifPresent(m -> constantUniforms.add(m.build(layerManager).uniforms()));

            final Uniforms frameUniforms = new Uniforms();
            frameUniforms.addPrimitiveUniform("position", point, (id, pt) -> GL43.glUniform3fv(id, pt.getPositionFloats()));
            frameUniforms.addPrimitiveUniform("model", point, (id, pt) -> GL43.glUniformMatrix4fv(id, false, pt.getModelFloats()));
            SimpleVao vao = SimpleVao.create(vertices);

            ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
            shaderProgram.init();
            shaderProgram.setConstantUniforms(constantUniforms);

            return new SimpleItem(vao, shaderProgram, frameUniforms, point);
        }

        public Builder setMaterial(final Material.Builder material) {
            this.material = Optional.ofNullable(material);
            return this;
        }

        public Builder setPoint(final Point point) {
            this.point = point;
            return this;
        }

        public Builder setTexture(final String texture) {
            this.texture = Optional.ofNullable(texture);
            return this;
        }
    }
}
