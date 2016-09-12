package com.wraith.wraithcamera.gpucomponents.gles;

import android.content.Context;

import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraBrightnessProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.Camera1977Program;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraAmaroProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraBrannanProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraCleamProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraEarlyBirdProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraInkwellProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraMeiFuRedProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraMeiShiProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraMildProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraSierraProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraToasterProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraXproiiProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraYourFaceProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraZoeProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraColorProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraContrastProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraMoiveStyleProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraOilPaintingProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraSaturationProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraSobelEdgeDetectProgram;
import com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms.CameraMeiFuMeiBaiProgram;

/**
 * Created by liuzongyang on 15/5/21.
 */
@SuppressWarnings("unused")
public class Texture2dProgramFactory {
    public static Texture2dProgram getTexture2dProgram(
            Context context, Texture2dProgram.FilterType filterType, int width, int height){
        Texture2dProgram program = null;
        switch (filterType){
            case FILTER_MOVIE:
                program = new CameraMoiveStyleProgram(context);
                break;
            case OIL_PAINTING:
                program = new CameraOilPaintingProgram(context, width, height);
                break;
            case BRIGHT_FILTER:
                program = new CameraBrightnessProgram(context, width, height);
                break;
            case CONTRAST_FILTER:
                program = new CameraContrastProgram(context, width, height);
                break;
            case SATURATION_FILTER:
                program = new CameraSaturationProgram(context, width, height);
                break;
            case COLOR_FILTER:
                program = new CameraColorProgram(context, width, height);
                break;
            case SOBEL_EDGE_DETECTIVE:
                program = new CameraSobelEdgeDetectProgram(context, width, height);
                break;
            case MEI_FU:
                program = new CameraMeiFuMeiBaiProgram(context, width, height);
                break;
            case FILTER_1977:
                program = new Camera1977Program(context);
                break;
            case FILTER_MEISHI:
                program = new CameraMeiShiProgram(context);
                break;
            case FILTER_YOURFACE:
                program = new CameraYourFaceProgram(context);
                break;
            case FILTER_XPROII:
                program = new CameraXproiiProgram(context);
                break;
            case FILTER_EARLY_BIRD:
                program = new CameraEarlyBirdProgram(context);
                break;
            case FILTER_BRANNAN:
                program = new CameraBrannanProgram(context);
                break;
            case FILTER_AMARO:
                program = new CameraAmaroProgram(context);
                break;
            case FILTER_INKWELL:
                program = new CameraInkwellProgram(context);
                break;
            case FILTER_TOASTER:
                program = new CameraToasterProgram(context);
                break;
            case FILTER_SIERRA:
                program = new CameraSierraProgram(context);
                break;
            case FILTER_MILD:
                program = new CameraMildProgram(context);
                break;
            case FILTER_CLEAM:
                program = new CameraCleamProgram(context);
                break;
            case FILTER_ZOE:
                program = new CameraZoeProgram(context);
                break;
            case MEI_FU_RED:
                program = new CameraMeiFuRedProgram(context, width, height);
                break;
            default:
                program = new Texture2dProgram(context);
                break;
        }
        return program;
    }
}
