#extension GL_OES_EGL_image_external : require
precision highp float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
#define KERNEL_SIZE #KERNEL_SIZE#
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform highp float uKernel[KERNEL_SIZE * 2 + 1];
uniform highp float uWOffset[KERNEL_SIZE * 2 + 1];
uniform highp float uSigmaColorLength;
uniform highp float width;
void main()
{
    highp float weight = 1.0;
    highp vec4 ocolor = texture2D(sTexture, vTextureCoord);
    highp vec4 result = ocolor;
    
//    for(int i = 0; i <= 2 * KERNEL_SIZE; i++){
//        highp vec3 sampleColor = texture2D(sTexture, vec2(vTextureCoord.x + (float(i) - float(KERNEL_SIZE))/width, vTextureCoord.y)).rgb;
//        highp float gaussWeightValue = uKernel[i];
//        highp float closeness = max(min(distance(sampleColor, ocolor.rgb) * uSigmaColorLength, 1.0), 0.0);
//        highp float sampleWeight = gaussWeightValue * (1.0 - closeness);
//        result.rgb += sampleColor * sampleWeight;
//        weight += sampleWeight;
//    }
    result.rgb = clamp(result.rgb / weight, 0.001, 0.999);
	gl_FragColor = result;
}