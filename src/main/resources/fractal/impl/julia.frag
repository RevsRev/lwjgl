#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform dvec4 coordInfo;
uniform vec4 backgroundColor;
uniform vec4 intermediateColor;
uniform vec4 setColor;
uniform int maxIterations;
uniform double cR;
uniform double cI;

out vec4 FragColor;

vec4 mixColors(vec4 first, vec4 second, float ratio) {
    return first + ratio * (second - first);
}

void main()
{
    //Some interesting starting points
//    double cR = -0.79;
//    double cI = 0.15;
//    double cR = -0.162;
//    double cI = 1.04;
//    double cR = 0.3;
//    double cI = -0.01;
//    double cR = -1.476;
//    double cI = 0.0;
//    double cR = -0.12;
//    double cI = 0.77;
//    double cR = 0.28;
//    double cI = 0.008;


    double originX = coordInfo.x;
    double originY = coordInfo.y;
    double xWidth = coordInfo.z;
    double yWidth = coordInfo.w;

    double zR = double((vertexPosition.x * xWidth) + originX);
    double zI = double((vertexPosition.y * yWidth) + originY);

    double normSquared = zR * zR + zI * zI;
    int i = 0;
    while (normSquared < 4 && i < maxIterations) {
        double prevZR = zR;
        zR = zR * zR - zI * zI + cR;
        zI = 2.0 * prevZR * zI + cI;
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