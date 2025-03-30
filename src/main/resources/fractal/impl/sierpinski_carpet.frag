#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform dvec4 coordInfo;
uniform vec4 backgroundColor;
uniform vec4 setColor;
uniform int maxIterations;

out vec4 FragColor;

void main()
{
    double originX = coordInfo.x;
    double originY = coordInfo.y;
    double xWidth = coordInfo.z;
    double yWidth = coordInfo.w;

    double x = double((vertexPosition.x * xWidth) + originX);
    double y = double((vertexPosition.y * yWidth) + originY);

    if (x < 0 || x > 1 || y < 0 || y > 1) {
        FragColor = vec4(0.3, 0.0, 0.0, 1.0);
        return;
    }

    int i = 0;
    while (i < maxIterations) {
        int xB3 = int(3*x);
        int yB3 = int(3*y);

        if (xB3 == 1 && yB3 == 1) {
            break;
        }

        x = 3 * x - xB3;
        y = 3 * y - yB3;
        i++;
    }

    float ratio = i * (1.0 / maxIterations);
    float r = backgroundColor.r + (setColor.r - backgroundColor.r) * ratio;
    float g = backgroundColor.g + (setColor.g - backgroundColor.g) * ratio;
    float b = backgroundColor.b + (setColor.b - backgroundColor.b) * ratio;
    float a = backgroundColor.a + (setColor.a - backgroundColor.a) * ratio;

    FragColor = vec4(r, g, b, a);
}