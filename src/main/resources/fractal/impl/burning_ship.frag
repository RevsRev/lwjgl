#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform dvec4 coordInfo;
uniform vec4 backgroundColor;
uniform vec4 intermediateColor;
uniform vec4 setColor;
uniform int maxIterations;

out vec4 FragColor;

vec4 mixColors(vec4 first, vec4 second, float ratio) {
    return first + ratio * (second - first);
}

void main()
{
    double zR = 0.0;
    double zI = 0.0;

    double originX = coordInfo.x;
    double originY = coordInfo.y;
    double xWidth = coordInfo.z;
    double yWidth = coordInfo.w;

    double cR = double((vertexPosition.x * xWidth) + originX);
    double cI = double((vertexPosition.y * yWidth) + originY);

    double normSquared = zR * zR + zI * zI;
    int i = 0;
    while (normSquared < 4 && i < maxIterations) {
        double prevZR = zR;
        zR = zR * zR - zI * zI + cR;
        zI = - 2.0 * abs(prevZR * zI) + cI;
        normSquared = zR * zR + zI * zI;
        i++;
    }

    float ratio = 1 - i * (1.0 / maxIterations);

    if (ratio < 0.5) {
        FragColor = mixColors(setColor, intermediateColor, ratio / 0.5);
        return;
    }
    FragColor = mixColors(intermediateColor, backgroundColor, (ratio - 0.5) / 0.5);
}