package com.wraith.wraithcamera.gpucomponents;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by liuzongyang on 15/5/28.
 */
public class FileHelper {
    public static boolean saveJpgToFile(Bitmap bitmap, String filePath){
        File file = new File(filePath);
        FileOutputStream out = null;
        try {
            if(file.exists() == false){
                file.createNewFile();
            }
            out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
