#version 430 core

in vec4 vertexPosition;

layout (location = 0) out vec3 position;
layout (location = 1) out vec3 velocity;

bool inCircle(vec4 testPoint, vec2 centre, float radius) {
    return (testPoint.x - centre.x) * (testPoint.x - centre.x) + (testPoint.y - centre.y) * (testPoint.y - centre.y) < radius * radius;
}

void main()
{
    position = vec3(0.0);
    velocity = vec3(0.0);

    if (inCircle(vertexPosition, vec2(0.0), 0.1f)) {
        position = vec3(1.0, 0.0, 0.0);
//        velocity = vec3(0.1, 0.0, 0.0);
    }
}