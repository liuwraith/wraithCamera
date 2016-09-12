package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/11/10.
 */
public class CameraZoeProgram extends Texture2dProgram {
    private static final String FragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2;\n" +
            "uniform float coffieRatio;\n"+
            "uniform mediump float shadow;\n" +
            "uniform mediump float highlight;\n" +
            "uniform mediump float contrast;\n" +
            "uniform mediump float saturation;\n" +
            "uniform mediump float brightness;\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.3, 0.3, 0.3);\n" +
            "const highp float EPSILON = 0.001;\n" +

            "mediump vec3 adjustBrightness(vec3 originalColor){\n" +
            "    return originalColor.rgb  + originalColor.rgb * ((1.0 / (1.0 - brightness)) - 1.0);\n" +
            "}\n" +
            "highp vec3 adjustShadowHighlight(highp vec3 originColor){\n" +
            "   mediump float luminanceValue = dot(originColor, luminanceWeighting);\n" +
            " 	mediump float shadowValue = clamp((pow(luminanceValue, 1.0/(abs(shadow)+1.0)) + (-0.76)*pow(luminanceValue, 2.0/(abs(shadow)+1.0))) - luminanceValue, 0.0, 1.0);\n" +
            " 	mediump float highlightValue = clamp((1.0 - (pow(1.0-luminanceValue, 1.0/(2.0-abs(1.0-abs(highlight)))) + (-0.6)*pow(1.0-luminanceValue, 2.0/(2.0-abs(1.0-abs(highlight)))))) - luminanceValue, -1.0, 0.0);\n" +
            "   shadowValue = sign(shadow) * pow(shadowValue, 0.65) - sign(-shadow) * pow(shadowValue, 0.65);\n"+
            "   highlightValue = sign(highlight) * abs(highlightValue) - sign(-highlight) * abs(highlightValue);\n"+
            "   highp vec3 result = originColor.rgb  + originColor.rgb * ((1.0 / (1.0 - (shadowValue + highlightValue))) - 1.0);\n"+

            "   return result;\n"+
            "}\n" +
            "highp vec3 adjustContrast(highp vec3 originalColor){\n" +
            "    return vec3(0.5, 0.5, 0.5) + (originalColor.rgb  - vec3(0.5, 0.5, 0.5)) * (1.0 + contrast);\n" +
            "}\n" +
            "highp vec3 adjustSaturation(highp vec3 originalColor){\n" +
            "     float max = max(max(originalColor.r, originalColor.g), originalColor.b);\n" +
            "    float min = min(min(originalColor.r, originalColor.g), originalColor.b);\n" +
            "    float delta = (max - min);\n" +
            "\n" +
            "    float addSum = (max + min);\n" +
            "    float l = addSum / 2.0;\n" +
            "    float s;\n" +
            "    vec3 result;\n" +
            "\n" +
            "    if(l < 0.5) {\n" +
            "        s = delta / addSum;\n" +
            "    } else {\n" +
            "        s = delta / (2.0 - addSum);\n" +
            "    }\n" +
            "\n" +
            "    float ratio;\n" +
            "    float raSaturatio = saturation * coffieRatio;\n"+
            "    if(saturation > 0.0) {\n" +
            "        if((saturation + s) >= 1.0){\n" +
            "            ratio = s;\n" +
            "        } else {\n" +
            "            ratio = 1.0 - saturation;\n" +
            "        }\n" +
            "\n" +
            "        ratio = 1.0/ratio - 1.0;\n" +
            "        result = originalColor.rgb + (originalColor.rgb - l) * ratio;\n" +
            "    } else {\n" +
            "        ratio = 1.0 + saturation;\n" +
            "\n" +
            "        result = vec3(l, l, l) + (originalColor.rgb - vec3(l, l, l)) *\n" +
            "            vec3(ratio, ratio, ratio);\n" +
            "    }\n"+
            "   return result;\n" +
            "}\n" +
            "highp float Luminance(highp vec3 color)\n" +
            "{\n" +
            "    highp float fmin = min(min(color.r, color.g), color.b);\n" +
            "    highp float fmax = max(max(color.r, color.g), color.b);\n" +
            "    return (fmax + fmin) / 2.0;\n" +
            "}\n" +
            "highp vec3 adjustCoffieStyle(highp vec3 originColor){\n"+
            "   highp vec3 result;"+
            "   result.r = texture2D(inputImageTexture2, vec2(originColor.r, 0.16666)).r;"+
            "   result.g = texture2D(inputImageTexture2, vec2(originColor.g, 0.5)).g;"+
            "   result.b = texture2D(inputImageTexture2, vec2(originColor.b, 0.83333)).b;"+
            "   return result;\n"+
            "}\n"+
            "void main() \n"+
            "{  highp vec4 textureColor = texture2D(inputImageTexture, vTextureCoord);\n" +
            "   highp float oneSubEpsilon = 1.0 - EPSILON;\n"+
            "   highp vec3 result = textureColor.rgb;\n"+
            "   result = clamp(adjustBrightness(result.rgb), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustContrast(result), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustSaturation(result), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustShadowHighlight(result), EPSILON, oneSubEpsilon);\n" +
            "   highp vec3 temp = textureColor.rgb;\n"+
            "   result = clamp(adjustCoffieStyle(result), EPSILON, oneSubEpsilon);\n"+
            "   result.b += clamp((result.g - result.b) * 0.25, EPSILON, oneSubEpsilon);\n"+
            "   result = mix(temp, result, coffieRatio);\n"+
            "   gl_FragColor = vec4(result, 1.0);\n" +
            "} \n" +
            "";


    //shadows: Increase to lighten shadows, from 0.0 to 1.0, with 0.0 as the default.
    private float mShadow = 0.0f;
    //highlights: Decrease to darken highlights, from 0.0 to 1.0, with 1.0 as the default.
    private float mHighLight = 0.0f;
    // from -1 to 1 default 0
    private float mContrast = -0.3f;
    //from -1 to 1 default 0
    private float mSaturation = -0.2f;
    //from 0 to 1 default 0
    private float mBrightness = 0.2f;

    private float mCoffiesRatio = 0.7f;

    private int mShadowLoc = 0;
    private int mHighLightLoc = 0;
    private int mContrastLoc = 0;
    private int mSaturationLoc = 0;
    private int mBrightnessLoc = 0;
    private int mCoffieRatioLoc = 0;
    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    public CameraZoeProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_ZOE;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FragmentShader);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.zoe);
        initParams();
    }

    @Override
    public void release(){
        super.release();
        if(mInputImageTexture2Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture2Handler}, 0);
            mInputImageTexture2Handler = -1;
        }

    }

    @Override
    public void initParams() {
        super.initParams();
        mShadowLoc = GLES20.glGetUniformLocation(mProgramHandle, "shadow");
        mHighLightLoc = GLES20.glGetUniformLocation(mProgramHandle, "highlight");
        mContrastLoc = GLES20.glGetUniformLocation(mProgramHandle, "contrast");
        mSaturationLoc = GLES20.glGetUniformLocation(mProgramHandle, "saturation");
        mBrightnessLoc = GLES20.glGetUniformLocation(mProgramHandle, "brightness");
        mCoffieRatioLoc = GLES20.glGetUniformLocation(mProgramHandle, "coffieRatio");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
    }

    private void setParam() {
        GLES20.glUniform1f(mShadowLoc, mShadow);
        GLES20.glUniform1f(mHighLightLoc, mHighLight);
        GLES20.glUniform1f(mContrastLoc, mContrast);
        GLES20.glUniform1f(mSaturationLoc, mSaturation);
        GLES20.glUniform1f(mBrightnessLoc, mBrightness);
        GLES20.glUniform1f(mCoffieRatioLoc, mCoffiesRatio);
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture2Handler);
        GLES20.glUniform1i(mInputImageTexture2Loc, 1);

        //set param
        setParam();

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);

        GLES20.glBindTexture(mDefaultTextureTarget, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }


}
