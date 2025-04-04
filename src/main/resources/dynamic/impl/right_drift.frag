#version 430 core
out vec4 FragColor;
in vec2 TexCoords;
uniform sampler2D screenTexture;
const float offset = 1.0 / 300.0;
void main()
{
    FragColor = vec4(vec3(texture(screenTexture, TexCoords.st + vec2(-offset, 0.0f))), 1.0);
}