#version 430 core
layout (location = 0) in vec3 aPos; // position has attribute position 0
out vec4 vertexPosition; // specify a color output to the fragment shader
void main()
{
    gl_Position = vec4(aPos, 1.0); // we give a vec3 to vec4’s constructor
    vertexPosition = vec4(aPos, 1.0); // output variable to dark-red
}