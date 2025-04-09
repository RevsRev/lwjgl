package github.com.rev.gl.shader;

import lombok.Getter;
import org.lwjgl.opengl.GL43;

import static github.com.rev.util.IOUtils.readCharSequence;

public class ShaderProgram {

    private final String vertexResource;
    private final String fragmentResource;

    @Getter
    private int programId;
    private boolean initialised = false;
    public ShaderProgram(final String vertexResource, final String fragmentResource) {
        this.vertexResource = vertexResource;
        this.fragmentResource = fragmentResource;
    }

    public void use(final Uniforms... nonConstantUniforms) {
        if (!initialised) {
            String message = String.format(
                    "Trying to use a shader before it has been initialised (vertex: '%s', fragment: '%s')",
                    vertexResource,
                    fragmentResource);
            throw new RuntimeException(message);
        }
        GL43.glUseProgram(programId);
        setUniforms(nonConstantUniforms);
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

        initialised = true;
    }

    public void setConstantUniforms(final Uniforms uniforms) {
        setUniforms(uniforms);
    }
    private void setUniforms(final Uniforms... uniforms) {
        if (!initialised) {
            throw new RuntimeException("Trying to set uniforms on an uninitialised shader");
        }
        GL43.glUseProgram(programId);
        int size = uniforms == null ? 0 : uniforms.length;
        for (int i = 0; i < size; i++) {
            uniforms[i].loadUniforms(programId);
        }
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
