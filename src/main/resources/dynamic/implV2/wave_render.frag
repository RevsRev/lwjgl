#version 430 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D inputPosition;
uniform sampler2D inputVelocity;

float toColor(float height) {
    return 1 - 1 / (1 + exp(height));
}

void main()
{
    vec3 col = texture(inputPosition, TexCoords).rgb;
    FragColor = vec4(toColor(col.x), toColor(col.x), toColor(col.x), 1.0f);
//    FragColor = vec4(col.x, 0.0, 0.0, 1.0f);
}