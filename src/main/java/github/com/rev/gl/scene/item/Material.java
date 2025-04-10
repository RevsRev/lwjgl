package github.com.rev.gl.scene.item;

import github.com.rev.gl.shader.Uniforms;
import github.com.rev.gl.texture.LayerManager;
import github.com.rev.gl.texture.image.ImageTexture;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class Material {
    private final LightingProperties ambient;
    private final LightingProperties diffuse;
    private final LightingProperties specular;
    private final LightingProperties shininess;

    private Material(final LightingProperties ambient,
                    final LightingProperties diffuse,
                    final LightingProperties specular,
                    final LightingProperties shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    public Uniforms uniforms() {
        final Uniforms u = new Uniforms();
        u.addStructUniform("material", this, Map.of(
                "ambient", (id, mat) -> mat.ambient.consumer.accept(id),
                "diffuse", (id, mat) -> mat.diffuse.consumer.accept(id),
                "specular", (id, mat) -> mat.specular.consumer.accept(id),
                "shininess", (id, mat) -> mat.shininess.consumer.accept(id)
        ));
        return u;
    }

    public static final class Builder {
        private Optional<String> diffuseLightingMap = Optional.empty();
        private Optional<String> ambientLightingMap = Optional.empty();
        private Optional<String> specularLightingMap = Optional.empty();
        private Vector3f diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        private Vector3f ambient = new Vector3f(1.0f, 1.0f, 1.0f);
        private Vector3f specular = new Vector3f(1.0f, 1.0f, 1.0f);
        private float shininess = 0.5f;

        public Material build(final LayerManager layerManager) {

            return new Material(
                    getLightingProperties(ambientLightingMap, ambient, layerManager),
                    getLightingProperties(diffuseLightingMap, diffuse, layerManager),
                    getLightingProperties(specularLightingMap, specular, layerManager),
                    new LightingProperties(shininess)
            );
        }

        private static LightingProperties getLightingProperties(final Optional<String> texture,
                                                                final Vector3f defaultValue,
                                                                final LayerManager layerManager) {
            return texture.map(
                            s -> new LightingProperties(ImageTexture.fromFile(s, 512, 512, layerManager.next())))
                    .orElseGet(() -> new LightingProperties(defaultValue));
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
            this.diffuseLightingMap = Optional.ofNullable(diffuseLightingMap);
            return this;
        }

        public Builder addAmbientLightingMap(final String ambientLightingMap) {
            this.ambientLightingMap = Optional.ofNullable(ambientLightingMap);
            return this;
        }

        public Builder addSpecularLightingMap(final String specularLightingMap) {
            this.specularLightingMap = Optional.ofNullable(specularLightingMap);
            return this;
        }
    }

    private static class LightingProperties {

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
