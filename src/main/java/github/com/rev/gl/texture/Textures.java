package github.com.rev.gl.texture;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Textures implements TextureOperations {

    private final Collection<Texture> textures;
    int[] layers = null;

    public Textures(Collection<Texture> textures) {
        this.textures = textures;
    }

    @Override
    public void init(int width, int height) {
        textures.forEach(t -> t.init(width, height));
    }

    @Override
    public void bindForReading() {
        textures.forEach(Texture::bindForReading);
    }

    @Override
    public int[] bindForWriting() {
        if (layers != null) {
            textures.forEach(Texture::bindForWriting);
            return layers;
        }

        final List<Integer> layersList = new ArrayList<>();
        textures.forEach(
                t -> {
                    final int[] layers = t.bindForWriting();
                    for (int layer : layers){
                        layersList.add(layer);
                    }
                }
        );
        layers = new int[layersList.size()];
        for (int i = 0; i < layersList.size(); i++) {
            layers[i] = layersList.get(i);
        }
        return layers;
    }

    @Override
    public void resize(int width, int height) {
        textures.forEach(t -> t.resize(width, height));
    }
}
