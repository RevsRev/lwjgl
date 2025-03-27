#version 430 core
in vec2 TexCoords;

layout (location = 0) out vec3 color;

uniform sampler2D screenTexture;

void main()
{
    color = vec3(0.0, 1.0, 0.0);
}