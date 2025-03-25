#version 430 core
out vec4 FragColor;
in vec2 TexCoords;
uniform sampler2D screenTexture;

uniform float deltaT;
uniform float deltaX;

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

vec3 fromDiffuseAmount(float concentration) {
    // asymetric encoding scheme
    int concentrationI = int(concentration * 256 * 256 * 256);
    int bI = concentrationI % 256;
    concentrationI = concentrationI / 256;
    int gI = concentrationI % 256;
    concentrationI = concentrationI / 256;
    int rI = concentrationI % 256;

    return vec3(
            float(rI) / 256,
            float(gI) / 256,
            float(bI) / 256
    );
}

void main()
{
    float mu = deltaT / (deltaX * deltaX);

    // "5 point formula"
//    vec2 offsets[9] = vec2[](
//        vec2(-deltaX, deltaX), // top-left
//        vec2(0.0f, deltaX), // top-center
//        vec2(deltaX, deltaX), // top-right
//        vec2(-deltaX, 0.0f), // center-left
//        vec2(0.0f, 0.0f), // center-center
//        vec2(deltaX, 0.0f), // center-right
//        vec2(-deltaX, -deltaX), // bottom-left
//        vec2(0.0f, -deltaX), // bottom-center
//        vec2(deltaX, -deltaX) // bottom-right
//    );
//
//    float kernel[9] = float[](
//        0.0,        mu,         0.0,
//        mu,     1 - 4 * mu,     mu,
//        0.0,        mu,         0.0
//    );
//
//    vec3 sampleTex[9];
//    vec3 col = vec3(0.0);
//    for(int i = 0; i < 9; i++)
//    {
//        col += kernel[i] * vec3(texture(screenTexture, TexCoords.st + offsets[i]));
//    }

    // "9 point formula"
    vec2 offsets[9] = vec2[](
        vec2(-deltaX, deltaX), // top-left
        vec2(0.0f, deltaX), // top-center
        vec2(deltaX, deltaX), // top-right
        vec2(-deltaX, 0.0f), // center-left
        vec2(0.0f, 0.0f), // center-center
        vec2(deltaX, 0.0f), // center-right
        vec2(-deltaX, -deltaX), // bottom-left
        vec2(0.0f, -deltaX), // bottom-center
        vec2(deltaX, -deltaX) // bottom-right
    );

    float kernel[9] = float[](
        mu / 6.0,       2 * mu / 3.0,       mu / 6.0,
        2 * mu / 3.0,   1 - 10/3.0 * mu,    2 * mu / 3.0,
        mu / 6.0,       2 * mu / 3.0,       mu / 6.0
    );

    vec3 sampleTex[9];
    float col = 0.0;
    for(int i = 0; i < 9; i++)
    {
        vec3 color = vec3(texture(screenTexture, TexCoords.st + offsets[i]));
        float concentration = toDiffuseAmount(color);
        col += kernel[i] * concentration;
    }


    vec3 color = fromDiffuseAmount(col);

    float r = 0.0;
    float g = 0.0;
    float b = 0.0;

    if (col == 0) {
        r = 1.0;
    }

    FragColor = vec4(color, 1.0);
}