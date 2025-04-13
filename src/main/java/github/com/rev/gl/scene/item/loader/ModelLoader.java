package github.com.rev.gl.scene.item.loader;

import github.com.rev.gl.scene.item.Model;
import github.com.rev.gl.texture.LayerManager;

public interface ModelLoader {
    Model load(String modelPath, LayerManager layerManager);
}
