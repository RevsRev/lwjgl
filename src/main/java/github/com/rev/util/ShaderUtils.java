package github.com.rev.util;

import org.lwjgl.opengl.GL43;

import static github.com.rev.util.IOUtils.readCharSequence;

public final class ShaderUtils {

    private ShaderUtils(){}

    public static int loadShader(final int shaderType, final String resourcePath) {
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

    public static int setupShaderProgram(final String vertexShaderPath, final String fragmentShaderPath) {
        int vertexShader = ShaderUtils.loadShader(GL43.GL_VERTEX_SHADER, vertexShaderPath);
        int fragmentShader = ShaderUtils.loadShader(GL43.GL_FRAGMENT_SHADER, fragmentShaderPath);

        int shaderProgram = GL43.glCreateProgram();
        GL43.glAttachShader(shaderProgram, vertexShader);
        GL43.glAttachShader(shaderProgram, fragmentShader);
        GL43.glLinkProgram(shaderProgram);


        int[] boostrapLinkStatus = new int[1];
        GL43.glGetProgramiv(shaderProgram, GL43.GL_LINK_STATUS, boostrapLinkStatus);
        if (boostrapLinkStatus[0] != 1) {
            System.out.println(GL43.glGetProgramInfoLog(shaderProgram));
        }
        GL43.glDeleteShader(vertexShader);
        GL43.glDeleteShader(fragmentShader);

        return shaderProgram;
    }

}
