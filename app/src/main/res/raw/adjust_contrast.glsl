#extension GL_OES_EGL_image_external : require
precision highp float;							// precision in the fragment shader.
uniform float u_contrastRatio;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
  
// The entry point for our fragment shader.
void main()                    		
{
    vec4 originalColor = texture2D(sTexture, vTextureCoord);
    vec3 newColor = vec3(0.5, 0.5, 0.5) +
        (originalColor.rgb  - vec3(0.5, 0.5, 0.5)) * u_contrastRatio;

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = vec4(newColor, originalColor.a); //(diffuse * );
  }                                                                     	

