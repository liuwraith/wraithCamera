#extension GL_OES_EGL_image_external : require
precision highp float;							// precision in the fragment shader.
uniform float u_luminanceRatio;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
// The entry point for our fragment shader.
void main()                    		
{
    vec4 originalColor = texture2D(sTexture, vTextureCoord);
    vec3 newColor;
    if(u_luminanceRatio < 0.0){
        newColor = originalColor.rgb + originalColor.rgb * u_luminanceRatio;
    } else {
        newColor = originalColor.rgb  + originalColor.rgb * ((1.0 / (1.0 - u_luminanceRatio)) - 1.0);
    }
	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = vec4(newColor, originalColor.a); //(diffuse * );
  }                                                                     	

