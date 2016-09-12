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
public class CameraXproiiProgram extends Texture2dProgram{
    public static final String XPROII_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
            + "precision highp float;\n"
            + "\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "\n"
            + "uniform highp float mixturePercent;\n"

            + "uniform samplerExternalOES inputImageTexture;\n"
            + "uniform sampler2D inputImageTexture2;\n"
            + "uniform sampler2D inputImageTexture3;\n"
            + "const highp float EPSILON = 0.001;\n"
            + "const highp float SUB_EPSLION = 0.999;\n"
                    + "\n"
            + "void main()\n"
            + "{\n"
            + "    \n"
            + "    highp vec3 texel = texture2D(inputImageTexture, vTextureCoord).rgb;\n"
            + "    highp vec3 texel2 = texture2D(inputImageTexture, vTextureCoord).rgb;\n"
            + "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"
            + "    texel2 = clamp(texel2, EPSILON, SUB_EPSLION);\n"
            + "    highp vec2 tc = (2.0 * vTextureCoord) - 1.0;\n"
            + "    highp float d = dot(tc, tc);\n"
            + "    highp vec2 lookup = vec2(d, texel.r);\n"
            + "    lookup = clamp(lookup, EPSILON, SUB_EPSLION);\n"
            + "    texel.r = texture2D(inputImageTexture3, lookup).r;\n"
            + "    lookup.y = texel.g;\n"
            + "    lookup = clamp(lookup, EPSILON, SUB_EPSLION);\n"
            + "    texel.g = texture2D(inputImageTexture3, lookup).g;\n"
            + "    lookup.y = texel.b;\n"
            + "    lookup = clamp(lookup, EPSILON, SUB_EPSLION);\n"
            + "    texel.b	= texture2D(inputImageTexture3, lookup).b;\n"
            + "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"
            + "    highp vec2 red = vec2(texel.r, 0.16666);\n"
            + "    red = clamp(red, EPSILON, SUB_EPSLION);\n"
            + "    highp vec2 green = vec2(texel.g, 0.5);\n"
            + "    green = clamp(green, EPSILON, SUB_EPSLION);\n"
            + "    highp vec2 blue = vec2(texel.b, .83333);\n"
            + "    blue = clamp(blue, EPSILON, SUB_EPSLION);\n"
            + "    highp vec3 texel3=vec3(\n"
            + "    texture2D(inputImageTexture2, red).r,\n"
            + "    texture2D(inputImageTexture2, green).g,\n"
            + "    texture2D(inputImageTexture2, blue).b);\n"
            + "    texel3 = clamp(texel3, EPSILON, SUB_EPSLION);\n"
            + "    gl_FragColor = vec4(mix(texel2, texel3, mixturePercent), 1.0);\n"
                    + "}";

    private float mMixturePercentage = 0.7f;
    private int mMixturePercentageLoc = -1;

    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    private int mInputImageTexture3Handler = -1;
    private int mInputImageTexture3Loc = -1;

    public CameraXproiiProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_XPROII;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, XPROII_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.xpro_map);
        mInputImageTexture3Handler = createTextureObject(context, R.mipmap.vignette_map);
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
    }

    @Override
    public void initParams() {
        super.initParams();
        mMixturePercentageLoc = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
        mInputImageTexture3Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture3");
    }

    private void setParam() {
        GLES20.glUniform1f(mMixturePercentageLoc, mMixturePercentage);
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
