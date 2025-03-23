#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

out vec4 FragColor;

void main()
{
    float r = 0.0f;
    float g = 1.0f;

//    if (vertexPosition.x < -0.5) {
//        r = 1.0f;
//        g = 0.0f;
//    }

    if (vertexPosition.x * vertexPosition.x + vertexPosition.y * vertexPosition.y < 0.1f) {
        r = 1.0f;
    }

    FragColor = vec4(r, g, 0.0f, 1.0);
}