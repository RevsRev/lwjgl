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

}
