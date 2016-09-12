#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform float uWidth;
uniform float uHeight;

void main()
{
    vec3 irgb = texture2D(sTexture, vTextureCoord).rgb;

    irgb = irgb.rgb  + irgb.rgb * ((1.0 / (1.0 - 0.6)) - 1.0);
    irgb = vec3(0.5, 0.5, 0.5) +
        (irgb.rgb  - vec3(0.5, 0.5, 0.5)) * 1.7;

    float max = max(max(irgb.r, irgb.g), irgb.b);
    float min = min(min(irgb.r, irgb.g), irgb.b);
    float delta = (max - min);

    float addSum = (max + min);
    float l = addSum / 2.0;
    float s;
    vec3 result;

    if(l < 0.5) {
        s = delta / addSum;
    } else {
        s = delta / (2.0 - addSum);
    }

    float ratio = 0.33;

    result = vec3(l, l, l) + (irgb.rgb - vec3(l, l, l)) *
    vec3(ratio, ratio, ratio);

    irgb = result;


    float ResS = uWidth;
    float ResT = uHeight;

    vec2 stp0 = vec2(1./ResS, 0.);
    vec2 st0p = vec2(0., 1./ResT);
    vec2 stpp = vec2(1./ResS, 1./ResT);
    vec2 stpm = vec2(1./ResS, -1./ResT);

    const vec3 W = vec3(0.2125, 0.7154, 0.0721);
    float i00 = dot(texture2D(sTexture, vTextureCoord).rgb, W);
    float im1m1 = dot(texture2D(sTexture, vTextureCoord - stpp).rgb, W);
    float ip1p1 = dot(texture2D(sTexture, vTextureCoord + stpp).rgb, W);
    float im1p1 = dot(texture2D(sTexture, vTextureCoord - stpm).rgb, W);
    float ip1m1 = dot(texture2D(sTexture, vTextureCoord + stpm).rgb, W);
    float im10 = dot(texture2D(sTexture, vTextureCoord - stp0).rgb, W);
    float ip10 = dot(texture2D(sTexture, vTextureCoord + stp0).rgb, W);
    float i0m1 = dot(texture2D(sTexture, vTextureCoord - st0p).rgb, W);
    float i0p1 = dot(texture2D(sTexture, vTextureCoord + st0p).rgb, W);
    float h = -1.*im1p1 - 2.*i0p1 - 1.*ip1p1 + 1.*im1m1 + 2.*i0m1 + 1.*ip1m1;
    float v = -1.*im1m1 - 2.*im10 - 1.*im1p1 + 1.*ip1m1 + 2.*ip10 + 1.*ip1p1;

    float mag = length(vec2(h, v));
    vec3 target = irgb;
    if(mag >= 0.2 && mag <= 0.4){
        target = vec3(0.5, 0.5, 0.5) * target;
    } else if(mag > 0.4){
        target = vec3(0.35, 0.32, 0.35) * target;
    } else if (mag >=0.1 && mag < 0.2){
        target = vec3(0.8, 0.8, 0.8) * target;
    } else {

    }

    gl_FragColor = vec4(target, 1.);
}