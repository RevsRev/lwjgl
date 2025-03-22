#version 430 core
out vec4 FragColor;
in vec4 vertexPosition; // input variable from vs (same name and type)
void main()
{
    //Mandelbrot set
    double zR = 0.0;
    double zI = 0.0;

    double cR = double(vertexPosition.x * 2.5);
    double cI = double(vertexPosition.y * 2.5);

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

    float color = i * (1.0 / limit);

    FragColor = vec4(color, color, color, 1.0);
}