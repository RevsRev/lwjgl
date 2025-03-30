#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform dvec4 coordInfo;
uniform vec4 backgroundColor;
uniform vec4 setColor;
uniform int maxIterations;

out vec4 FragColor;

const double sin60 = sqrt(3.0)/2.0;
const double root3 = sqrt(3.0);

void main()
{
    double originX = coordInfo.x;
    double originY = coordInfo.y;
    double xWidth = coordInfo.z;
    double yWidth = coordInfo.w;

    double xOrthog = double((vertexPosition.x * xWidth) + originX);
    double yOrthog = double((vertexPosition.y * yWidth) + originY);

    double x = xOrthog - yOrthog / root3;
    double y = yOrthog / sin60;

    if (x < 0 || y < 0 || x + y > 1) {
        FragColor = backgroundColor;
        return;
    }

    int i = 0;
    while (i < maxIterations) {
        int xB2 = int(2*x);
        int yB2 = int(2*y);

        if (xB2 == 1 && yB2 == 1) {
            break;
        }

        x = 2 * x - xB2;
        y = 2 * y - yB2;
        i++;
    }

//    float ratio = i * (1.0 / maxIterations);

    float ratio = 0.0;
    if (i == maxIterations) {
        ratio = 1.0;
    }

    float r = backgroundColor.r + (setColor.r - backgroundColor.r) * ratio;
    float g = backgroundColor.g + (setColor.g - backgroundColor.g) * ratio;
    float b = backgroundColor.b + (setColor.b - backgroundColor.b) * ratio;
    float a = backgroundColor.a + (setColor.a - backgroundColor.a) * ratio;

    FragColor = vec4(r, g, b, a);
}