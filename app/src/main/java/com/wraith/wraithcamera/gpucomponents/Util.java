package com.wraith.wraithcamera.gpucomponents;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.FaceDetector;

import java.nio.ByteBuffer;

/**
 * Created by liuzongyang on 15/5/28.
 */
public class Util {
    public static void convertRightToLeftBitmap(ByteBuffer buffer, int width, int height, int stride){

        buffer.rewind();
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width / 2; j++){
                int index_left = width * height + stride * j;
                int index_right = width * height + stride * (width - j - 1);
                for(int k = 0; k < 4; k++){
                    byte a = buffer.get(index_left + k);
                    buffer.put(index_left + k, buffer.get(index_right + k));
                    buffer.put(index_right + k, a);
                }
            }
        }
        buffer.rewind();

    }

    public static float dp2pix(Context cxt, float dp){
        float density = cxt.getResources().getDisplayMetrics().density;
        return (density * dp + 0.5f);
    }

    public static FaceDetector.Face[] detectFaceInBitmapBytes(byte[] bytes, int width, int height){
        Bitmap mfoto_imm = getBitmapFromNV21(bytes, width, height, true);  //here I get the Bitmap from getBitmapFromNV21 that is the conversion method
        Bitmap mfoto= mfoto_imm.copy(Bitmap.Config.RGB_565, true);
        FaceDetector mface= new FaceDetector(height, width, 1);
        FaceDetector.Face [] face= new FaceDetector.Face[1];
        int count = mface.findFaces(mfoto, face);
        return face;
    }

    public static Bitmap getBitmapFromNV21(byte[] data, int width, int height, boolean rotated) {

        Bitmap bitmap = null;
        int[] pixels = new int[width * height];

        // Conver the  array
        yuv2rgb(pixels, data, width, height, rotated);


        if(rotated)
        {
            bitmap = Bitmap.createBitmap(pixels, height, width, Bitmap.Config.RGB_565);
        }
        else
        {
            bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
        }

        return bitmap;
    }


    public static void yuv2rgb(int[] out, byte[] in, int width, int height, boolean rotated)
            throws NullPointerException, IllegalArgumentException
    {
        final int size = width * height;


        if(out == null) throw new NullPointerException("buffer 'out' == null");
        if(out.length < size) throw new IllegalArgumentException("buffer 'out' length < " + size);
        if(in == null) throw new NullPointerException("buffer 'in' == null");
        if(in.length < (size * 3 / 2)) throw new IllegalArgumentException("buffer 'in' length != " + in.length + " < " + (size * 3/ 2));

        // YCrCb
        int Y, Cr = 0, Cb = 0;


        int Rn = 0, Gn = 0, Bn = 0;
        for(int j = 0, pixPtr = 0, cOff0 = size - width; j < height; j++) {
            if((j & 0x1) == 0)
                cOff0 += width;
            int pixPos = height - 1 - j;
            for(int i = 0, cOff = cOff0; i < width; i++, cOff++, pixPtr++, pixPos += height) {

                // Get Y
                Y = 0xff & in[pixPtr]; // 0xff es por el signo

                // Get Cr y Cb
                if((pixPtr & 0x1) == 0) {
                    Cr = in[cOff];
                    if(Cr < 0) Cr += 127; else Cr -= 128;
                    Cb = in[cOff + 1];
                    if(Cb < 0) Cb += 127; else Cb -= 128;

                    Bn = Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                    Gn = - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                    Rn = Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                }


                int R = Y + Rn;
                if(R < 0) R = 0; else if(R > 255) R = 255;
                int B = Y + Bn;
                if(B < 0) B = 0; else if(B > 255) B = 255;
                int G = Y + Gn;
                if(G < 0) G = 0; else if(G > 255) G = 255; //At this point the code could apply some filter From the separate components of the image.For example, they could swap 2 components or remove one

                int rgb = 0xff000000 | (R << 16) | (G << 8) | B; //Depending on the option the output buffer is filled or not applying the transformation
                if(rotated)
                    out[pixPos] = rgb;
                else
                    out[pixPtr] = rgb;
            }
        }
    }
}
