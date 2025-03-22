#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform vec4 coordInfo;
uniform vec4 backgroundColor;
uniform vec4 setColor;
uniform int maxIterations;

out vec4 FragColor;

void main()
{
    //Mandelbrot set
    double zR = 0.0;
    double zI = 0.0;

    float originX = coordInfo.x;
    float originY = coordInfo.y;
    float xWidth = coordInfo.z;
    float yWidth = coordInfo.w;

    double cR = double((vertexPosition.x * xWidth) + originX);
    double cI = double((vertexPosition.y * yWidth) + originY);

    double normSquared = zR * zR + zI * zI;
    int i = 0;
    while (normSquared < 4 && i < maxIterations) {
        double prevZR = zR;
        zR = zR * zR - zI * zI + cR;
        zI = 2.0 * prevZR * zI + cI;
        normSquared = zR * zR + zI * zI;
        i++;
    }

    float ratio = i * (1.0 / maxIterations);
    float r = backgroundColor.r + (setColor.r - backgroundColor.r) * ratio;
    float g = backgroundColor.g + (setColor.g - backgroundColor.g) * ratio;
    float b = backgroundColor.b + (setColor.b - backgroundColor.b) * ratio;
    float a = backgroundColor.a + (setColor.a - backgroundColor.a) * ratio;

    FragColor = vec4(r, g, b, a);
}