package github.com.rev.util;

import github.com.rev.Mandelbrot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils {

    private IOUtils(){}

    public static CharSequence readCharSequence(String resourcePath) {
        try (InputStream is = Mandelbrot.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException(String.format("Failed to load resource '%s'", resourcePath));
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = is.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to load resource '%s'", resourcePath), e);
        }
    }

}
