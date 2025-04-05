package github.com.rev.gl.texture.buffered;

import github.com.rev.gl.shader.ShaderReadable;
import github.com.rev.gl.texture.ops.Resizable;
import github.com.rev.gl.shader.ShaderWritable;

public interface BufferedTexture extends ShaderReadable, ShaderWritable, Resizable {
    void init(int width, int height);
}
