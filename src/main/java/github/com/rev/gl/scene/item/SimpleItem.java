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
        final float[] vertices;
        final String vertexShader;
        final String fragmentShader;
        final String texture;
        private Optional<Material.Builder> material = Optional.empty();

        public Builder(float[] vertices, String vertexShader, String fragmentShader, String texture) {
            this.vertices = vertices;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
            this.texture = texture;
        }

        public SimpleItem build(final LayerManager layerManager) {

            final ImageTexture imageTexture = ImageTexture.fromFile(texture, 512, 512, layerManager.next()); //TODO - Extract to parameters!
            imageTexture.init();

            Uniforms constantUniforms = new Uniforms();
            constantUniforms.addPrimitiveUniform("aTexture", imageTexture, (id, tex) -> {
                imageTexture.bindForReading(id);
            });

            material.ifPresent(m -> constantUniforms.add(m.build(layerManager).uniforms()));

            final Point point = new Point(new Axes(), new Position());

            final Uniforms frameUniforms = new Uniforms();
            frameUniforms.addPrimitiveUniform("position", point, (id, pt) -> GL43.glUniform3fv(id, pt.getPositionFloats()));
            frameUniforms.addPrimitiveUniform("model", point, (id, pt) -> GL43.glUniformMatrix4fv(id, false, pt.getModelFloats()));
            SimpleVao vao = SimpleVao.create(vertices);

            ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
            shaderProgram.init();
            shaderProgram.setConstantUniforms(constantUniforms);

            return new SimpleItem(vao, shaderProgram, frameUniforms, point);
        }

        public Builder setMaterial(Material.Builder material) {
            this.material = Optional.ofNullable(material);
            return this;
        }
    }
}
