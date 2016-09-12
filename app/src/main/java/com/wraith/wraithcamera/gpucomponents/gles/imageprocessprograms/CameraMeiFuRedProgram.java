package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.RawResourceReader;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/11/16.
 */
public class CameraMeiFuRedProgram extends Texture2dProgram {

    private int mWidth;
    private int mHeight;

    private int mVerticalFBOProgramHandle;
    private int mHorizontalFBOProgramHandle;

    private final static int GAUSS_KERNEL_SIZE = 0;

    private float DOMAIN_SIGMA = 25.72f;
    private float COLOR_SIGMA = 1.0f;

    //bilateral vertical frame buffer ref
    protected int mBilateralVerticalOffScreenTexture;
    protected int mBilateralVerticalFramebuffer;

    //bilateral horizontal frame buffer ref
    protected int mBilateralHorizontalOffScreenTexture;
    protected int mBilateralHorizontalFramebuffer;

    //双边滤波垂直滤波后的纹理location
    private int muVerticalGaussTextureLoc;
    //双边滤波水平滤波后的纹理location
    private int muHorizontalGaussTextureLoc;

    //双边滤波水平滤波参数
    private int muBilatoralHorizentalKernelLoc;
    private int muBilatoralHorizentalOffsetLoc;
    private int muBilatoralHorizentalPositionLoc;
    private int muBilatoralHorizentalTextureCoordLoc;
    private int muBilatoralHorizentalMVPMatrixLoc;
    private int muBilatoralHorizentalTexMatrixLoc;
    private int muBilateralHorizontalColorSigmaLoc;

    //双边滤波垂直滤波参数
    private int muBilatoralVerticalPositionLoc;
    private int muBilatoralVerticalTextureCoordLoc;
    private int muBilatoralVerticalMVPMatrixLoc;
    private int muBilatoralVerticalTexMatrixLoc;
    private int muBilatoralVerticalKernelLoc;
    private int muBilatoralVerticalOffsetLoc;
    private int muBilateralVerticalColorSigmaLoc;

    //锐化参数
    private int maSharpPositionLoc;
    private int maSharpTextureCoordLoc;
    private int muSharpMVPMatrixLoc;
    private int muSharpTexMatrixLoc;
    private int mRedTextureLoc;

    private int mRedTextureId;

    private float[] muBilatoralKernel = new float[GAUSS_KERNEL_SIZE * 2 + 1];
    private float[] muBilatoralWOffset = new float[GAUSS_KERNEL_SIZE * 2 + 1];
    private float[] muBilatoralHOffset = new float[GAUSS_KERNEL_SIZE * 2 + 1];

    private FloatBuffer FULL_RECTANGLE_TEX_BUF;

    public CameraMeiFuRedProgram(Context context, int width, int height){
        mWidth = width;
        mHeight = height;
        mFilterType = FilterType.MEI_FU_RED;
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        //TODO
        String sharpFragment = RawResourceReader.readTextFileFromRawResource(context, R.raw.red_mei_fu);
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, sharpFragment);
        if(mProgramHandle == 0){
            throw new RuntimeException("Unable to create sharp filter");
        }

        //TODO
        String toonFragment = RawResourceReader.readTextFileFromRawResource(context, R.raw.horizental_gauss_blur_line);
        toonFragment = toonFragment.replace("#KERNEL_SIZE#", Integer.toString(GAUSS_KERNEL_SIZE));
        mHorizontalFBOProgramHandle = GlUtil.createProgram(VERTEX_SHADER,toonFragment);

        if(mHorizontalFBOProgramHandle == 0){
            throw new RuntimeException("Unable to create program!");
        }

        //TODO
        String horizontal_gauss_blur = RawResourceReader.readTextFileFromRawResource(context, R.raw.vertical_gauss_blur_line);
        horizontal_gauss_blur = horizontal_gauss_blur.replace("#KERNEL_SIZE#", Integer.toString(GAUSS_KERNEL_SIZE));
        mVerticalFBOProgramHandle = GlUtil.createProgram(VERTEX_SHADER, horizontal_gauss_blur
        );
        if(mVerticalFBOProgramHandle == 0){
            throw new RuntimeException("Unable to create program!");
        }

        GlUtil.checkGlError("meifu filter release!");

        float FULL_RECTANGLE_TEX_COORDS[] = {
                0.0f, 0.0f,     // 0 bottom left
                1.0f, 0.0f,     // 1 bottom right
                0.0f, 1.0f,     // 2 top left
                1.0f, 1.0f      // 3 top right
        };
        FULL_RECTANGLE_TEX_BUF =
                GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
        FULL_RECTANGLE_TEX_BUF.rewind();

        getLocationOfHorizontalBilFBOAUP();
        getLocationOfVerticalBilFBOAUP();
        getLocationOfSharpAUP();

        prepareVerticalGaussFramebuffer(width, height);
        GlUtil.checkGlError("meifu filter release!");
        prepareHorizontalGaussFramebuffer(width, height);
        GlUtil.checkGlError("meifu filter release!");
        prepareGaussKernel();
        prepareGaussOffset();
        prepareRedTextrue(context);
    }

    private void prepareRedTextrue(Context context){
        mRedTextureId = createTextureObject(context, R.mipmap.red_color);

        mRedTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "texture1");
    }

    private void prepareGaussKernel(){
        for(int i = -GAUSS_KERNEL_SIZE; i <= GAUSS_KERNEL_SIZE; i++){
            muBilatoralKernel[i + GAUSS_KERNEL_SIZE] = (float)Math.exp(-(i * i) / (2.0f * (GAUSS_KERNEL_SIZE / 2.0f) * (GAUSS_KERNEL_SIZE / 2.0f)));
        }
    }

    private void prepareGaussOffset(){
        for(int i = - GAUSS_KERNEL_SIZE; i <= GAUSS_KERNEL_SIZE; i++){
            muBilatoralHOffset[i + GAUSS_KERNEL_SIZE] = i / (float)mHeight;
            muBilatoralWOffset[i + GAUSS_KERNEL_SIZE] = i / (float)mWidth;
        }
    }

    @Override
    public void setRatio(RatioType type, float value){
        if(type == RatioType.Default){
            DOMAIN_SIGMA = value * 49.0f + 1.0f;
            prepareGaussKernel();
        } else if(type == RatioType.First){
            COLOR_SIGMA = value;
        }
    }

    @Override
    public float getRatio(RatioType type){
        if(type == RatioType.First){
            return (COLOR_SIGMA - 1.0f)/49.0f;
        } else if(type == RatioType.Default){
            return DOMAIN_SIGMA;
        }
        return mRatio;
    }
    private int muHeightLoc;
    private int muWidthLoc;

    private void getLocationOfVerticalBilFBOAUP(){
        muBilatoralVerticalPositionLoc = GLES20.glGetAttribLocation(mVerticalFBOProgramHandle, "aPosition");

        muBilatoralVerticalTextureCoordLoc = GLES20.glGetAttribLocation(mVerticalFBOProgramHandle, "aTextureCoord");

        muBilatoralVerticalMVPMatrixLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "uMVPMatrix");

        muBilatoralVerticalTexMatrixLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "uTexMatrix");

        muBilatoralVerticalKernelLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "uKernel");

        muBilatoralVerticalOffsetLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "uHOffset");

        muHeightLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "height");

        muBilateralVerticalColorSigmaLoc = GLES20.glGetUniformLocation(mVerticalFBOProgramHandle, "uSigmaColorLength");
    }

    // get locations of attributes and uniforms
    protected void getLocationOfHorizontalBilFBOAUP(){
        muBilatoralHorizentalPositionLoc = GLES20.glGetAttribLocation(mHorizontalFBOProgramHandle, "aPosition");

        muBilatoralHorizentalTextureCoordLoc = GLES20.glGetAttribLocation(mHorizontalFBOProgramHandle, "aTextureCoord");

        muBilatoralHorizentalMVPMatrixLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "uMVPMatrix");

        muBilatoralHorizentalTexMatrixLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "uTexMatrix");

        muVerticalGaussTextureLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "sTexture");
        muBilatoralHorizentalKernelLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "uKernel");
        muBilatoralHorizentalOffsetLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "uWOffset");
        muBilateralHorizontalColorSigmaLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "uSigmaColorLength");
        muWidthLoc = GLES20.glGetUniformLocation(mHorizontalFBOProgramHandle, "width");
    }


    private void getLocationOfSharpAUP(){
        maSharpPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");

        maSharpTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");

        muSharpMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");

        muSharpTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");

        muHorizontalGaussTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "sTexture");
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareVerticalGaussFramebuffer(int width, int height) {
        int[] values = new int[1];
        GLES20.glGenFramebuffers(1, values, 0);
        mBilateralVerticalFramebuffer = values[0];    // expected > 0

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);

        mBilateralVerticalOffScreenTexture = values[0];   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBilateralVerticalOffScreenTexture);

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        setTextureParam();

        // Create framebuffer object and bind it.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mBilateralVerticalFramebuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mBilateralVerticalOffScreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void setTextureParam(){
        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareHorizontalGaussFramebuffer(int width, int height) {
        int[] values = new int[1];

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        mBilateralHorizontalFramebuffer = values[0];    // expected > 0

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        mBilateralHorizontalOffScreenTexture = values[0];   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBilateralHorizontalOffScreenTexture);

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        setTextureParam();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mBilateralHorizontalFramebuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mBilateralHorizontalOffScreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void drawSharp(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                           int vertexCount, int coordsPerVertex, int vertexStride, int texStride){
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBilateralHorizontalOffScreenTexture);
        GLES20.glUniform1i(muHorizontalGaussTextureLoc, 0);
        GlUtil.checkGlError("meifu filter release!");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRedTextureId);
        GLES20.glUniform1i(mRedTextureLoc, 1);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muSharpMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muSharpTexMatrixLoc, 1, false, mTexMatrix, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maSharpPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maSharpPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maSharpTextureCoordLoc);

        FULL_RECTANGLE_TEX_BUF.rewind();
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maSharpTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, FULL_RECTANGLE_TEX_BUF);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maSharpPositionLoc);
        GLES20.glDisableVertexAttribArray(maSharpTextureCoordLoc);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private void drawVerticalGauss(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                                   int vertexCount, int coordsPerVertex, int vertexStride,
                                   float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride){
        // Select the program.
        GLES20.glUseProgram(mVerticalFBOProgramHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mBilateralVerticalFramebuffer);

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);
        GlUtil.checkGlError("meifu filter release!");

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muBilatoralVerticalMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muBilatoralVerticalTexMatrixLoc, 1, false, texMatrix, 0);

        GLES20.glUniform1fv(muBilatoralVerticalKernelLoc, GAUSS_KERNEL_SIZE * 2 + 1, muBilatoralKernel, 0);

        GLES20.glUniform1fv(muBilatoralVerticalOffsetLoc, GAUSS_KERNEL_SIZE * 2 + 1, muBilatoralHOffset, 0);

        GLES20.glUniform1f(muBilateralVerticalColorSigmaLoc, COLOR_SIGMA);
        GLES20.glUniform1f(muHeightLoc, mHeight);
        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(muBilatoralVerticalPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(muBilatoralVerticalPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(muBilatoralVerticalTextureCoordLoc);

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(muBilatoralVerticalTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(muBilatoralVerticalPositionLoc);
        GLES20.glDisableVertexAttribArray(muBilatoralVerticalTextureCoordLoc);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(mDefaultTextureTarget, 0);
        GLES20.glUseProgram(0);

    }

    private final static float[] mTexMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f};

    private void drawHorizontalGauss(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                                     int vertexCount, int coordsPerVertex, int vertexStride, int texStride){
        // Select the program.
        GLES20.glUseProgram(mHorizontalFBOProgramHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mBilateralHorizontalFramebuffer);

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBilateralVerticalOffScreenTexture);
        GLES20.glUniform1i(muVerticalGaussTextureLoc, 0);
        GlUtil.checkGlError("meifu filter release!");

        GLES20.glUniform1f(muBilateralHorizontalColorSigmaLoc, COLOR_SIGMA);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muBilatoralHorizentalMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muBilatoralHorizentalTexMatrixLoc, 1, false, mTexMatrix, 0);

        GLES20.glUniform1fv(muBilatoralHorizentalKernelLoc, GAUSS_KERNEL_SIZE * 2 + 1, muBilatoralKernel, 0);

        GLES20.glUniform1fv(muBilatoralHorizentalOffsetLoc, GAUSS_KERNEL_SIZE * 2 + 1, muBilatoralWOffset, 0);
        GLES20.glUniform1f(muWidthLoc, mWidth);
        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(muBilatoralHorizentalPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(muBilatoralHorizentalPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(muBilatoralHorizentalTextureCoordLoc);

        FULL_RECTANGLE_TEX_BUF.rewind();
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(muBilatoralHorizentalTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, FULL_RECTANGLE_TEX_BUF);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(muBilatoralHorizentalPositionLoc);
        GLES20.glDisableVertexAttribArray(muBilatoralHorizentalTextureCoordLoc);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }


    private void drawSharp(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                           int vertexCount, int coordsPerVertex,
                           int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride, int textureId){
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBilateralHorizontalOffScreenTexture);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);
        GLES20.glUniform1i(muHorizontalGaussTextureLoc, 0);
        GlUtil.checkGlError("meifu filter release!");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRedTextureId);
        GLES20.glUniform1i(mRedTextureLoc, 1);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muSharpMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muSharpTexMatrixLoc, 1, false, texMatrix, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maSharpPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maSharpPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maSharpTextureCoordLoc);

        FULL_RECTANGLE_TEX_BUF.rewind();
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maSharpTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maSharpPositionLoc);
        GLES20.glDisableVertexAttribArray(maSharpTextureCoordLoc);

        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindTexture(mDefaultTextureTarget, 0);
        GLES20.glUseProgram(0);
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        drawVerticalGauss(mvpMatrix, vertexBuffer, firstVertex, vertexCount, coordsPerVertex,
                vertexStride, texMatrix, texBuffer, textureId, texStride);

        vertexBuffer.rewind();
        texBuffer.rewind();

        float[] newMvpMatrix = new float[16];
        Matrix.setIdentityM(newMvpMatrix, 0);

        drawHorizontalGauss(newMvpMatrix, vertexBuffer, firstVertex, vertexCount, coordsPerVertex,
                vertexStride, texStride);
        vertexBuffer.rewind();
        texBuffer.rewind();
        drawSharp(newMvpMatrix, vertexBuffer,firstVertex,vertexCount,coordsPerVertex,vertexStride,texStride);
    }

    @Override
    public void release(){
        int[] values = new int[1];

        //双边滤波垂直相关
        if (mBilateralVerticalOffScreenTexture > 0) {
            values[0] = mBilateralVerticalOffScreenTexture;
            GLES20.glDeleteTextures(1, values, 0);
            mBilateralVerticalOffScreenTexture = -1;
        }
        if (mBilateralVerticalFramebuffer > 0) {
            values[0] = mBilateralVerticalFramebuffer;
            GLES20.glDeleteFramebuffers(1, values, 0);
            mBilateralVerticalFramebuffer = -1;
        }
        //双边滤波水平相关
        if (mBilateralHorizontalOffScreenTexture > 0) {
            values[0] = mBilateralHorizontalOffScreenTexture;
            GLES20.glDeleteTextures(1, values, 0);
            mBilateralHorizontalOffScreenTexture = -1;
        }
        if (mBilateralHorizontalFramebuffer > 0) {
            values[0] = mBilateralHorizontalFramebuffer;
            GLES20.glDeleteFramebuffers(1, values, 0);
            mBilateralHorizontalFramebuffer = -1;
        }

        Log.d(TAG, "deleting program " + mVerticalFBOProgramHandle);
        GLES20.glDeleteProgram(mVerticalFBOProgramHandle);
        mVerticalFBOProgramHandle = -1;

        Log.d(TAG, "deleting program " + mHorizontalFBOProgramHandle);
        GLES20.glDeleteProgram(mHorizontalFBOProgramHandle);
        mHorizontalFBOProgramHandle = -1;

        super.release();

        GlUtil.checkGlError("meifu filter release!");
    }
}
