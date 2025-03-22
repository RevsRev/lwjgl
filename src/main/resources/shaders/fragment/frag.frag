#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

uniform float zoom;
uniform vec2 mousePos;
uniform vec4 backgroundColor;
uniform vec4 setColor;

out vec4 FragColor;

void main()
{
    //Mandelbrot set
    double zR = 0.0;
    double zI = 0.0;

    double cR = double(vertexPosition.x * zoom);
    double cI = double(vertexPosition.y * zoom);

    double normSquared = zR * zR + zI * zI;
    int i = 0;
    int limit = 100;
    while (normSquared < 4 && i < limit) {
        double prevZR = zR;
        zR = zR * zR - zI * zI + cR;
        zI = 2.0 * prevZR * zI + cI;
        normSquared = zR * zR + zI * zI;
        i++;
    }

    float ratio = i * (1.0 / limit);
    float r = backgroundColor.r + (setColor.r - backgroundColor.r) * ratio;
    float g = backgroundColor.g + (setColor.g - backgroundColor.g) * ratio;
    float b = backgroundColor.b + (setColor.b - backgroundColor.b) * ratio;
    float a = backgroundColor.a + (setColor.a - backgroundColor.a) * ratio;

    FragColor = vec4(r, g, b, a);
}