package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/11/9.
 */
public class CameraEarlyBirdProgram extends Texture2dProgram{
    private int mMixLocation = -1;
    private float mMix = 0.7f;
    public static final String EARLYBIRD_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "\n" +
            "uniform highp float mixturePercent;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2; //earlyBirdCurves\n" +
            "uniform sampler2D inputImageTexture3; //earlyBirdOverlay\n" +
            "uniform sampler2D inputImageTexture4; //vig\n" +
            "uniform sampler2D inputImageTexture5; //earlyBirdBlowout\n" +
            "uniform sampler2D inputImageTexture6; //earlyBirdMap\n" +
            "const highp float EPSILON = 0.001;\n"+
            "const highp float SUB_EPSLION = 0.999;\n"+
                    "\n" +
            "const highp mat3 saturate = mat3(\n" +
            "                           1.210300,\n" +
            "                           -0.089700,\n" +
            "                           -0.091000,\n" +
            "                           -0.176100,\n" +
            "                           1.123900,\n" +
            "                           -0.177400,\n" +
            "                           -0.034200,\n" +
            "                           -0.034200,\n" +
            "                           1.265800);\n" +
            "const highp vec3 rgbPrime = vec3(0.25098, 0.14640522, 0.0); \n" +
            "const highp vec3 desaturate = vec3(.3, .59, .11);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    \n" +
            "    highp vec3 texel = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "    highp vec3 texel2 = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    texel2 = clamp(texel2, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    \n" +
            "    highp vec2 lookup;    \n" +
            "    lookup.y = 0.5;\n" +
            "    \n" +
            "    lookup.x = texel.r;\n" +
            "    texel.r = texture2D(inputImageTexture2, lookup).r;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    lookup.x = texel.g;\n" +
            "    texel.g = texture2D(inputImageTexture2, lookup).g;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    lookup.x = texel.b;\n" +
            "    texel.b = texture2D(inputImageTexture2, lookup).b;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    highp float desaturatedColor;\n" +
            "    highp vec3 result;\n" +
            "    desaturatedColor = dot(desaturate, texel);\n" +
            "    \n" +
            "    \n" +
            "    lookup.x = desaturatedColor;\n" +
            "    result.r = texture2D(inputImageTexture3, lookup).r;\n" +
            "    result = clamp(result, EPSILON, SUB_EPSLION);\n"+
            "    lookup.x = desaturatedColor;\n" +
            "    result.g = texture2D(inputImageTexture3, lookup).g;\n" +
            "    result = clamp(result, EPSILON, SUB_EPSLION);\n"+
            "    lookup.x = desaturatedColor;\n" +
            "    result.b = texture2D(inputImageTexture3, lookup).b;\n" +
            "    result = clamp(result, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    texel = saturate * mix(texel, result, .5);\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    highp vec2 tc = (2.0 * vTextureCoord) - 1.0;\n" +
            "    highp float d = dot(tc, tc);\n" +
            "    \n" +
            "    highp vec3 sampled;\n" +
            "    lookup.y = .5;\n" +
            "    \n" +
            "    /*\n" +
            "     lookup.x = texel.r;\n" +
            "     sampled.r = texture2D(inputImageTexture4, lookup).r;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "     \n" +
            "     lookup.x = texel.g;\n" +
            "     sampled.g = texture2D(inputImageTexture4, lookup).g;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "     \n" +
            "     lookup.x = texel.b;\n" +
            "     sampled.b = texture2D(inputImageTexture4, lookup).b;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "     \n" +
            "     highp float value = smoothstep(0.0, 1.25, pow(d, 1.35)/1.65);\n" +
            "     texel = mix(texel, sampled, value);\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "     */\n" +
            "    \n" +
            "    //---\n" +
            "    \n" +
            "    lookup = vec2(d, texel.r);\n" +
            "    lookup = clamp(lookup, EPSILON, SUB_EPSLION);\n"+
            "    texel.r = texture2D(inputImageTexture4, lookup).r;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    lookup.y = texel.g;\n" +
            "    texel.g = texture2D(inputImageTexture4, lookup).g;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    lookup.y = texel.b;\n" +
            "    texel.b	= texture2D(inputImageTexture4, lookup).b;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    highp float value = smoothstep(0.0, 1.25, pow(d, 1.35)/1.65);\n" +
            "    \n" +
            "    //---\n" +
            "    \n" +
            "    lookup.x = texel.r;\n" +
            "    sampled.r = texture2D(inputImageTexture5, lookup).r;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "    lookup.x = texel.g;\n" +
            "    sampled.g = texture2D(inputImageTexture5, lookup).g;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "    lookup.x = texel.b;\n" +
            "    sampled.b = texture2D(inputImageTexture5, lookup).b;\n" +
            "    sampled = clamp(sampled, EPSILON, SUB_EPSLION);\n"+
            "    texel = mix(sampled, texel, value);\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    \n" +
            "    lookup.x = texel.r;\n" +
            "    texel.r = texture2D(inputImageTexture6, lookup).r;\n" +
            "    lookup.x = texel.g;\n" +
            "    texel.g = texture2D(inputImageTexture6, lookup).g;\n" +
            "    lookup.x = texel.b;\n" +
            "    texel.b = texture2D(inputImageTexture6, lookup).b;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    gl_FragColor = vec4(mix(texel2,texel,mixturePercent),1.0);\n"+
            "}";

    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    private int mInputImageTexture3Handler = -1;
    private int mInputImageTexture3Loc = -1;

    private int mInputImageTexture4Handler = -1;
    private int mInputImageTexture4Loc = -1;

    private int mInputImageTexture5Handler = -1;
    private int mInputImageTexture5Loc = -1;

    private int mInputImageTexture6Handler = -1;
    private int mInputImageTexture6Loc = -1;

    public CameraEarlyBirdProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_EARLY_BIRD;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, EARLYBIRD_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.earlybird_curves);
        mInputImageTexture3Handler = createTextureObject(context, R.mipmap.earlybird_overlay_map);
        mInputImageTexture4Handler = createTextureObject(context, R.mipmap.vignette_map);
        mInputImageTexture5Handler = createTextureObject(context, R.mipmap.earlybird_blowout);
        mInputImageTexture6Handler = createTextureObject(context, R.mipmap.earlybird_map);
        initParams();
    }

    @Override
    public void release(){
        super.release();
        if(mInputImageTexture2Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture2Handler}, 0);
            mInputImageTexture2Handler = -1;
        }
        if(mInputImageTexture3Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture3Handler}, 0);
            mInputImageTexture3Handler = -1;
        }
        if(mInputImageTexture4Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture4Handler}, 0);
            mInputImageTexture4Handler = -1;
        }
        if(mInputImageTexture5Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture5Handler}, 0);
            mInputImageTexture5Handler = -1;
        }
        if(mInputImageTexture6Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture6Handler}, 0);
            mInputImageTexture6Handler = -1;
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        mMixLocation = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
        mInputImageTexture3Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture3");
        mInputImageTexture4Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture4");
        mInputImageTexture5Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture5");
        mInputImageTexture6Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture6");
    }

    private void setParam() {
        GLES20.glUniform1f(mMixLocation, mMix);
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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture3Handler);
        GLES20.glUniform1i(mInputImageTexture3Loc, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture4Handler);
        GLES20.glUniform1i(mInputImageTexture4Loc, 3);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture5Handler);
        GLES20.glUniform1i(mInputImageTexture5Loc, 4);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture6Handler);
        GLES20.glUniform1i(mInputImageTexture6Loc, 5);
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
