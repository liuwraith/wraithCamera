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
public class CameraInkwellProgram extends Texture2dProgram{
    private int mMixLocation = -1;
    private float mMix = 0.7f;
    public static final String INKWELL_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2;\n" +
            "uniform highp float mixturePercent;\n"+
            "const highp float EPSILON = 0.001;\n"+
            "const highp float SUB_EPSLION = 0.999;\n"+
            "\n" +
            "void main()\n" +
            "{\n" +
            "    highp vec3 texel = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "    highp vec3 texel2 = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    texel2 = clamp(texel2, EPSILON, SUB_EPSLION);\n"+
            "    texel = vec3(dot(vec3(0.3, 0.6, 0.1), texel));\n" +
            "    texel = vec3(texture2D(inputImageTexture2, vec2(texel.r, .16666)).r);\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    gl_FragColor = vec4(mix(texel2,texel,mixturePercent), 1.0);\n"+
            "}";


    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    public CameraInkwellProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_INKWELL;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, INKWELL_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.inkwell_map);
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
        mMixLocation = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
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
