#version 430 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;
out vec4 vertexPosition; // specify a color output to the fragment shader
void main()
{
    gl_Position = vec4(aPos, 0.0, 1.0); // we give a vec3 to vec4’s constructor
    vertexPosition = vec4(aPos, 0.0, 1.0); // output variable to dark-red
}