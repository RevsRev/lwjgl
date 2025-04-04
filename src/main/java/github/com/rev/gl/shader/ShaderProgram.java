package github.com.rev.gl.shader;

import github.com.rev.gl.uniform.Uniform;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.Collection;

import static github.com.rev.util.IOUtils.readCharSequence;

public class ShaderProgram {

    private final String vertexResource;
    private final String fragmentResource;

    private final Collection<Uniform> constantUniforms = new ArrayList<>();
    private final Collection<Uniform> nonConstantUniforms = new ArrayList<>();

    private int programId;
    private boolean initialised = false;

    public ShaderProgram(final String vertexResource, final String fragmentResource) {
        this.vertexResource = vertexResource;
        this.fragmentResource = fragmentResource;
    }

    public void addUniform(final Uniform uniform) {
        if (uniform.isConstant()) {
            if (initialised) {
                String message = String.format(
                        "Trying to add a constant uniform after a shader has been initialised (vertex: '%s', fragment: '%s')",
                        vertexResource,
                        fragmentResource);
                throw new RuntimeException(message);
            }
            constantUniforms.add(uniform);
        } else {
            nonConstantUniforms.add(uniform);
        }
    }

    public void use() {
        if (!initialised) {
            String message = String.format(
                    "Trying to use a shader before it has been initialised (vertex: '%s', fragment: '%s')",
                    vertexResource,
                    fragmentResource);
            throw new RuntimeException(message);
        }
        GL43.glUseProgram(programId);
        loadUniforms(nonConstantUniforms);
    }

    public void init() {
        int vertexShader = loadShader(GL43.GL_VERTEX_SHADER, vertexResource);
        int fragmentShader = loadShader(GL43.GL_FRAGMENT_SHADER, fragmentResource);

        programId = GL43.glCreateProgram();
        GL43.glAttachShader(programId, vertexShader);
        GL43.glAttachShader(programId, fragmentShader);
        GL43.glLinkProgram(programId);

        int[] boostrapLinkStatus = new int[1];
        GL43.glGetProgramiv(programId, GL43.GL_LINK_STATUS, boostrapLinkStatus);
        if (boostrapLinkStatus[0] != 1) {
            System.out.printf("Vertex: %s%nFragment: %s%n", vertexResource, fragmentResource);
            System.out.println(GL43.glGetProgramInfoLog(programId));
        }
        GL43.glDeleteShader(vertexShader);
        GL43.glDeleteShader(fragmentShader);

        loadUniforms(constantUniforms);

        initialised = true;
    }

    private void loadUniforms(final Collection<Uniform> uniforms) {
        GL43.glUseProgram(programId);
        uniforms.forEach(u -> u.bind(programId));
    }

    private int loadShader(final int shaderType, final String resourcePath) {
        int shaderIdentifier = GL43.glCreateShader(shaderType);
        GL43.glShaderSource(shaderIdentifier, readCharSequence(resourcePath));
        GL43.glCompileShader(shaderIdentifier);

        int[] compileStatus = new int[1];
        GL43.glGetShaderiv(shaderIdentifier, GL43.GL_COMPILE_STATUS, compileStatus);

        if (compileStatus[0] != 1) {
            System.out.print(GL43.glGetShaderInfoLog(shaderIdentifier));
        }
        return shaderIdentifier;
    }

}
