precision highp float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
#define KERNEL_SIZE #KERNEL_SIZE#
varying highp vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform highp float uKernel[KERNEL_SIZE * 2 + 1];
uniform highp float uWOffset[KERNEL_SIZE * 2 + 1];
uniform highp float uSigmaColorLength;
const highp float EPSILON = 0.00000001;
highp mat3 param = mat3(65.481, 128.553, 24.966, -37.7745, -74.1592, 111.9337, 111.9581, -93.7509, -18.2072);

vec3 rgb2hsv(highp vec3 c)
{
    highp vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    highp vec4 p = mix(vec4(c.bg, k.wz), vec4(c.gb, k.xy), step(c.b, c.g));
    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    highp float d = q.x - min(q.w, q.y);
    highp float e = 0.00000001;
    highp float h = abs(q.z + (q.w - q.y)/(6.0*d + e));
    highp float s = d/(q.x + e);
    highp vec3 result = vec3(h, s, q.x);
    return result;
}
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 adjustSaturation(highp vec3 originalColor)
{
    highp float max = max(max(originalColor.r, originalColor.g), originalColor.b);
    highp float min = min(min(originalColor.r, originalColor.g), originalColor.b);
    highp float delta = (max - min);
    
    highp float addSum = (max + min);
    highp float l = addSum / 2.0;
    highp vec3 result;
    
    highp float ratio = 0.8;
    
    result = vec3(l, l, l) + (originalColor.rgb - vec3(l, l, l)) *
        vec3(ratio, ratio, ratio);
    return result;
}

float gaussWeight(highp float currentPos,highp float delta)
{
    return exp(-(currentPos * currentPos) / (2.0 * delta * delta));
}

void main()
{
    //find the face region
    highp float weight = 0.0;
    highp vec4 ocolor = texture2D(sTexture, vec2(vTextureCoord.x, vTextureCoord.y));
    highp vec4 result = vec4(ocolor.rgb, 0.0);
    highp vec3 hsvValue = rgb2hsv(result.rgb);
    if(hsvValue.r > 0.01 && hsvValue.r <= 50.0 && hsvValue.g >= 0.1 && hsvValue.g < 0.63 && hsvValue.b > 0.015){
        highp vec3 originColor = vec3(0.0, 0.0, 0.0);
        result = vec4(0.0, 0.0, 0.0, 0.0);
        for(int i = 0; i < 2 * KERNEL_SIZE + 1; i++){
            highp vec3 sampleColor = texture2D(sTexture, vec2(vTextureCoord.x + uWOffset[i], vTextureCoord.y)).rgb;
            highp float gaussWeightValue = uKernel[i];
            highp float closeness = gaussWeight(distance(sampleColor, ocolor.rgb), uSigmaColorLength);
            highp float sampleWeight = gaussWeightValue * closeness;
            originColor += sampleColor * sampleWeight;
            weight += sampleWeight;
        }
        result = vec4(originColor/weight, 1.0);
        result.rgb = result.rgb * vec3(1.0, 1.02, 1.02);
    }

    gl_FragColor = result;
}