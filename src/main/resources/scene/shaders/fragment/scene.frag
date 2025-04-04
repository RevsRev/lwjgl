#version 430 core
out vec4 FragColor;
in vec2 TexCoords;

struct PointLight {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

uniform PointLight[16] pointLights;
uniform sampler2D aTexture;

void main()
{
    FragColor = texture(aTexture, TexCoords);
}