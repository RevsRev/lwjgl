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

uniform PointLight[16] pointLights;
uniform DirLight[16] dirLights;
uniform sampler2D aTexture;

out vec4 FragColor;

vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir)
{
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),
                     /**material.shininess*/ 1.2f);
    // combine results
//    vec3 ambient = light.ambient * vec3(texture(material.diffuse,
//                                                TexCoords));
//    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse,
//                                                       TexCoords));
//    vec3 specular = light.specular * spec * vec3(texture(material.specular,
//                                                         TexCoords));

    vec3 ambient = light.ambient * vec3(texture(aTexture,
                                                TexCoords));
    vec3 diffuse = light.diffuse * diff * vec3(texture(aTexture,
                                                       TexCoords));
    vec3 specular = light.specular * spec * vec3(texture(aTexture,
                                                         TexCoords));

    return (ambient + diffuse + specular);
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3
viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),
                     /**material.shininess*/ 1.2f);
    // attenuation
    float aDistance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * aDistance +
    light.quadratic * (aDistance * aDistance));

    // combine results
    vec3 ambient = light.ambient * vec3(texture(aTexture,
                                                TexCoords));
    vec3 diffuse = light.diffuse * diff * vec3(texture(aTexture,
                                                       TexCoords));
    vec3 specular = light.specular * spec * vec3(texture(aTexture,
                                                         TexCoords));

//    vec3 ambient = light.ambient;
//    vec3 diffuse = light.diffuse;
//    vec3 specular = light.specular;

    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
}

void main()
{
    DirLight dirLight = dirLights[0];
    PointLight pointLight = pointLights[0];
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(FragPos);

    vec3 result = CalcDirLight(dirLight, norm, viewDir);
    result = result +  CalcPointLight(pointLight, norm, FragPos, viewDir);

//    FragColor = texture(aTexture, TexCoords);
    FragColor = vec4(result, 1.0f);
}