#version 430 core
out vec4 FragColor;
in vec2 TexCoords;
uniform sampler2D screenTexture;

float toDiffuseAmount(vec3 rgb)
{
    int r = int(rgb.r * 256);
    int g = int(rgb.g * 256);
    int b = int(rgb.b * 256);

    // asymetric encoding scheme
    int concentration = 0;
    concentration += r;
    concentration *= 256;
    concentration += g;
    concentration *= 256;
    concentration += b;

    return float(concentration) / (256 * 256 * 256);
}

void main()
{
    vec3 col = texture(screenTexture, TexCoords).rgb;
    float diffuse = toDiffuseAmount(col);
    FragColor = vec4(diffuse, 0.0, 0.0, 1.0f);
}