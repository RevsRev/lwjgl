package github.com.rev.gl.uniform;

import java.util.function.Consumer;

public class UniformPrimative extends Uniform {

    private final Consumer<Integer> setter;

    public UniformPrimative(String name, boolean constant, Consumer<Integer> setter) {
        super(name, constant);
        this.setter = setter;
    }

    @Override
    public void bind(int id) {
        setter.accept(id);
    }
}
