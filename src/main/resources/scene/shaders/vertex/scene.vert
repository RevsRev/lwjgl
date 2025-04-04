#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 TexCoords;
out vec3 Normal;
out vec3 FragPos;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    TexCoords = vec2(aTexCoords.x, aTexCoords.y);
    Normal = mat3(transpose(inverse(model))) * aNormal;
    FragPos = vec3(gl_Position.x, gl_Position.y, gl_Position.z);
}
