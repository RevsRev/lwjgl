#version 430 core

in vec4 vertexPosition; // input variable from vs (same name and type)

out vec4 FragColor;

vec3 fromDiffuseAmount(float concentration) {
    // asymetric encoding scheme
    int concentrationI = int(concentration * 256 * 256 * 256);
    int bI = concentrationI % 256;
    concentrationI = concentrationI / 256;
    int gI = concentrationI % 256;
    concentrationI = concentrationI / 256;
    int rI = concentrationI % 256;

    return vec3(
    float(rI) / 256,
    float(gI) / 256,
    float(bI) / 256
    );
}

bool inCircle(vec4 testPoint, vec2 centre, float radius) {
    return (testPoint.x - centre.x) * (testPoint.x - centre.x) + (testPoint.y - centre.y) * (testPoint.y - centre.y) < radius * radius;
}

void main()
{
    vec3 color = fromDiffuseAmount(0.0);

    if (inCircle(vertexPosition, vec2(0.0), 0.5f)) {
        color = fromDiffuseAmount(0.999);
    }

//    if (inCircle(vertexPosition, vec2(0.25,0.25), 0.1f)) {
//        color = fromDiffuseAmount(0.999); //integer overflow if we set this to 1.0 :))
//    }
//    if (inCircle(vertexPosition, vec2(0.25,-0.25), 0.1f)) {
//        color = fromDiffuseAmount(0.999); //integer overflow if we set this to 1.0 :))
//    }
//    if (inCircle(vertexPosition, vec2(-0.25,0.25), 0.1f)) {
//        color = fromDiffuseAmount(0.999); //integer overflow if we set this to 1.0 :))
//    }
//    if (inCircle(vertexPosition, vec2(-0.25,-0.25), 0.1f)) {
//        color = fromDiffuseAmount(0.999); //integer overflow if we set this to 1.0 :))
//    }

    FragColor = vec4(color, 1.0);
}