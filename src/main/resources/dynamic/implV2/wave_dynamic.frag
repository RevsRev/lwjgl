#version 430 core

in vec2 TexCoords;

layout (location = 0) out vec3 position;
layout (location = 1) out vec3 velocity;

uniform sampler2D inputPosition;
uniform sampler2D inputVelocity;

uniform float deltaT;
uniform float deltaX;
uniform vec2 offsets[9];
uniform float kernel[9];

void main()
{
    velocity = vec3(texture(inputVelocity, TexCoords.st));
    position = vec3(texture(inputPosition, TexCoords.st)) + deltaT * vec3(texture(inputVelocity, TexCoords.st));
    for(int i = 0; i < 9; i++)
    {
        vec3 gradDt = kernel[i] * vec3(texture(inputPosition, TexCoords.st + offsets[i]));
        velocity += gradDt;
        position += deltaT * gradDt / 2;
    }
}