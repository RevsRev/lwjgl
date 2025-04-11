#version 430 core
in vec2 TexCoords;
in vec3 Normal;
in vec3 FragPos;

struct PointLight {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct DirLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform int ambientTexturesSize;
uniform int diffuseTexturesSize;
uniform int specularTexturesSize;

uniform sampler2D[4] ambientTextures;
uniform sampler2D[4] diffuseTextures;
uniform sampler2D[4] specularTextures;
uniform float shininess = 1.0f;

uniform PointLight[16] pointLights;
uniform DirLight[16] dirLights;

out vec4 FragColor;

vec3 calcLight(float light, sampler2D tex) {
    vec3 diffuse = vec3(0.0f);
    for (int i = 0; i < 4; i++) {
        diffuse += light * vec3(texture(tex, TexCoords));
    }
    return diffuse;
}
vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),
                     shininess);

    vec3 lightSum = vec3(0.0f);

    for (int i = 0; i < min(ambientTexturesSize, 4); i++) {
        lightSum += light.ambient * vec3(texture(ambientTextures[i], TexCoords));
    }

    for (int i = 0; i < min(diffuseTexturesSize, 4); i++) {
        lightSum += light.diffuse * diff * vec3(texture(diffuseTextures[i], TexCoords));
    }

    for (int i = 0; i < min(specularTexturesSize, 4); i++) {
        lightSum += light.specular * spec * vec3(texture(specularTextures[i], TexCoords));
    }

    return lightSum;
}
vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
    // attenuation
    float aDistance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * aDistance +
    light.quadratic * (aDistance * aDistance));

    // combine results
    vec3 lightSum = vec3(0.0f);
    for (int i = 0; i < min(ambientTexturesSize, 4); i++) {
        lightSum += light.ambient * vec3(texture(ambientTextures[i], TexCoords));
    }

    for (int i = 0; i < min(diffuseTexturesSize, 4); i++) {
        lightSum += light.diffuse * diff * vec3(texture(diffuseTextures[i], TexCoords));
    }

    for (int i = 0; i < min(specularTexturesSize, 4); i++) {
        lightSum += light.specular * spec * vec3(texture(specularTextures[i], TexCoords));
    }

    return lightSum;
}

void main()
{
    DirLight dirLight = dirLights[0];
    PointLight pointLight = pointLights[0];
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(FragPos);

    vec3 result = calcDirLight(dirLight, norm, viewDir);
    result = result +  calcPointLight(pointLight, norm, FragPos, viewDir);

    FragColor = vec4(result, 1.0f);
}