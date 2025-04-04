#version 430 core
//layout (location = 0) in vec3 aPos;
//layout (location = 1) in vec3 aNormal;
//layout (location = 2) in vec2 aTexCoords;

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform mat4 transform;

out vec2 TexCoords;

void main()
{
    gl_Position = transform * vec4(aPos, 1.0);
    TexCoords = vec2(aTexCoords.x, aTexCoords.y);
}
