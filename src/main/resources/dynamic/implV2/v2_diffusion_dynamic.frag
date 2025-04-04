#version 430 core

in vec2 TexCoords;

layout (location = 0) out vec3 color;

uniform sampler2D screenTexture;

uniform float deltaT;
uniform float deltaX;

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

//     "9 point formula"
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
    color = vec3(0.0, 0.0, 0.0);
    for(int i = 0; i < 9; i++)
    {
        color += kernel[i] * vec3(texture(screenTexture, TexCoords.st + offsets[i]));
    }
}