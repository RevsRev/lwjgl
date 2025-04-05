package github.com.rev.gl.uniform;

import github.com.rev.gl.shader.ShaderReadable;
import lombok.Getter;

@Getter
public abstract class Uniform implements ShaderReadable {

    private final String name;
    private final boolean constant;

    public Uniform(String name, boolean constant) {
        this.name = name;
        this.constant = constant;
    }
}
