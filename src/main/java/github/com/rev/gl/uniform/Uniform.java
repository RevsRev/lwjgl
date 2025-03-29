package github.com.rev.gl.uniform;

import lombok.Getter;

@Getter
public abstract class Uniform {

    private final String name;
    private final boolean constant;

    public Uniform(String name, boolean constant) {
        this.name = name;
        this.constant = constant;
    }

    public abstract void bind(final int id);
}
