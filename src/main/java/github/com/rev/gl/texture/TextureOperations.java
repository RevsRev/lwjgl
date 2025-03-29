package github.com.rev.gl.texture;

public interface TextureOperations {
    void init(int width, int height);

    void bindForReading();

    int[] bindForWriting();

    void resize(int width, int height);
}
