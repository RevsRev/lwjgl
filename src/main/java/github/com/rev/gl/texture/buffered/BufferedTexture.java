package github.com.rev.gl.texture.buffered;

import github.com.rev.gl.texture.ops.Readable;
import github.com.rev.gl.texture.ops.Resizable;
import github.com.rev.gl.texture.ops.Writable;

public interface BufferedTexture extends Readable, Writable, Resizable {
    void init(int width, int height);
}
