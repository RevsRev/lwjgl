#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

out vec4 FragColor;

void main()
{
    FragColor = vec4(vertexPosition.r * vertexPosition.r, vertexPosition.g * vertexPosition.g, vertexPosition.b * vertexPosition.b, 1.0);
}