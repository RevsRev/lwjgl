#version 330 core
out vec4 FragColor;
in vec2 vertexPosition; // input variable from vs (same name and type)
void main()
{
    float zR = 0.0;
    float zI = 0.0;

    float cR = vertexPosition.x;
    float cI = vertexPosition.y;

    for(int i=0;i<50;++i)
    {
        zR = zR * zR - zI * zI + cR;
        zI = 2 * zR * zI + cI;
    }

    float normSquared = zR * zR + zI * zI;
    float color = 1.0;
    if (normSquared < 4) {
        color = 0.0;
    }

    FragColor = vec4(color, color, color, 1.0);
}