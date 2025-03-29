#version 430 core

in vec4 vertexPosition;

layout (location = 0) out vec3 color;

bool inCircle(vec4 testPoint, vec2 centre, float radius) {
    return (testPoint.x - centre.x) * (testPoint.x - centre.x) + (testPoint.y - centre.y) * (testPoint.y - centre.y) < radius * radius;
}

void main()
{
    color = vec3(0.0);

    if (inCircle(vertexPosition, vec2(0.0), 1.0f)) {
        color = vec3(0.0, 1.0, 0.0);
    }
}