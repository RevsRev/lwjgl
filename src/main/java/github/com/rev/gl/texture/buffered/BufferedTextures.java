package github.com.rev.gl.texture.buffered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BufferedTextures implements BufferedTexture {

    private final Collection<BufferedTexture> textures;
    int[] layers = null;

    public BufferedTextures(Collection<BufferedTexture> textures) {
        this.textures = textures;
    }

    @Override
    public void init(int width, int height) {
        textures.forEach(t -> t.init(width, height));
    }

    @Override
    public void bindForReading(int shaderProgram) {
        textures.forEach(bt -> bt.bindForReading(shaderProgram));
    }

    @Override
    public int[] bindForWriting() {
        if (layers != null) {
            textures.forEach(BufferedTexture::bindForWriting);
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
