package github.com.rev.gl.scene.item;

import github.com.rev.gl.scene.Axes;
import github.com.rev.gl.scene.Point;
import github.com.rev.gl.scene.Position;
import github.com.rev.gl.shader.ShaderProgram;
import github.com.rev.gl.shader.Uniforms;
import github.com.rev.gl.texture.LayerManager;
import github.com.rev.gl.texture.image.ImageTexture;
import github.com.rev.gl.uniform.Uniform;
import lombok.Getter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
        private final Collection<Uniform> uniforms = new HashSet<>();
        private Optional<String> diffuseLightingMap = Optional.empty();
        private Optional<String> ambientLightingMap = Optional.empty();
        private Optional<String> specularLightingMap = Optional.empty();
        private Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        private Vector3f ambient = new Vector3f(1.0f, 1.0f, 1.0f);
        private Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);
        private float shininess = 0.5f;

        public Builder(float[] vertices, String vertexShader, String fragmentShader, String texture) {
            this.vertices = vertices;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
            this.texture = texture;
        }

        public SimpleItem build() {
            final LayerManager layerManager = new LayerManager();

            final ImageTexture imageTexture = ImageTexture.fromFile(texture, 512, 512, layerManager.next()); //TODO - Extract to parameters!
            imageTexture.init();

            final Material material = new Material(
                    getLightingProperties(ambientLightingMap, ambient, layerManager),
                    getLightingProperties(diffuseLightingMap, diffuse, layerManager),
                    getLightingProperties(specularLightingMap, specular, layerManager),
                    getLightingProperties(Optional.empty(), shininess, layerManager)
            );

            Uniforms constantUniforms = new Uniforms();
            constantUniforms.addPrimitiveUniform("aTexture", imageTexture, (id, tex) -> {
                imageTexture.bindForReading(id);
            });
            constantUniforms.addStructUniform("material", material, Map.of(
                    "ambient", (id, mat) -> mat.ambient.consumer.accept(id),
                    "diffuse", (id, mat) -> mat.diffuse.consumer.accept(id),
                    "specular", (id, mat) -> mat.specular.consumer.accept(id),
                    "shininess", (id, mat) -> mat.shininess.consumer.accept(id)
            ));

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

        private static LightingProperties getLightingProperties(final Optional<String> lightingMap,
                                                                final Vector3f defaultValue,
                                                                final LayerManager layerManager) {
            if (lightingMap.isPresent()) {
                ImageTexture lightingTexture = ImageTexture.fromFile(lightingMap.get(), 512, 512, layerManager.next());
                return new LightingProperties(lightingTexture);
            }
            return new LightingProperties(defaultValue);
        }
        private static LightingProperties getLightingProperties(final Optional<String> lightingMap,
                                                                final float defaultValue,
                                                                final LayerManager layerManager) {
            if (lightingMap.isPresent()) {
                ImageTexture lightingTexture = ImageTexture.fromFile(lightingMap.get(), 512, 512, layerManager.next());
                return new LightingProperties(lightingTexture);
            }
            return new LightingProperties(defaultValue);
        }

        public Builder addUniform(final Uniform uniform) {
            uniforms.add(uniform);
            return this;
        }

        public Builder addDiffuseLighting(final Vector3f diffuse) {
            this.diffuse = diffuse;
            return this;
        }

        public Builder addAmbientLighting(final Vector3f ambient) {
            this.ambient = ambient;
            return this;
        }

        public Builder addSpecularLighting(final Vector3f specular) {
            this.specular = specular;
            return this;
        }

        public Builder addDiffuseLightingMap(final String diffuseLightingMap) {
            this.diffuseLightingMap = Optional.of(diffuseLightingMap);
            return this;
        }

        public Builder addAmbientLightingMap(final String ambientLightingMap) {
            this.ambientLightingMap = Optional.of(ambientLightingMap);
            return this;
        }

        public Builder addSpecularLightingMap(final String specularLightingMap) {
            this.specularLightingMap = Optional.of(specularLightingMap);
            return this;
        }
    }

    private static class Material {
        public final LightingProperties ambient;
        public final LightingProperties diffuse;
        public final LightingProperties specular;
        public final LightingProperties shininess;

        public Material(final LightingProperties ambient,
                        final LightingProperties diffuse,
                        final LightingProperties specular,
                        final LightingProperties shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }
    }

    private static class LightingProperties {

        @Getter
        private final Consumer<Integer> consumer;

        public LightingProperties(final Vector3f value) {
            this.consumer = id -> GL43.glUniform3f(id, value.x, value.y, value.z);
        }

        public LightingProperties(final float value) {
            this.consumer = id -> GL43.glUniform1f(id, value);
        }

        public LightingProperties(final ImageTexture texture) {
            this.consumer = texture::bindForReading;
        }
    }
}
