package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES20;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.RawResourceReader;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/5/29.
 */
public class CameraContrastProgram extends Texture2dProgram {
    private int muRatioLoc;

    private int mWidth;
    private int mHeight;
    private static final int mRadius = 8;

    public CameraContrastProgram(Context context, int width, int height){
        super();
        mWidth = width;
        mHeight = height;
        mFilterType = FilterType.CONTRAST_FILTER;
        //TODO
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER,
                RawResourceReader.readTextFileFromRawResource(context, R.raw.adjust_contrast));

        if(mProgramHandle == 0){
            throw new RuntimeException("Unable to create program!");
        }
        initParams();
        mRatio = 1.0f;
    }

    // get locations of attributes and uniforms
    protected void initParams(){
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        muRatioLoc = GLES20.glGetUniformLocation(mProgramHandle, "u_contrastRatio");
    }

    @Override
    public void setRatio(RatioType type, float ratio){
        if(type == RatioType.Default){
            mRatio = 2.0f * ratio;
        }
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);

        GLES20.glUniform1f(muRatioLoc, mRatio);

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
