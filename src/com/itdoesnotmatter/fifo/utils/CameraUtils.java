package com.itdoesnotmatter.fifo.utils;

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;

public class CameraUtils {

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	
	/** Check if this device has a camera */
	public static boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	// Camera.getNumberOfCameras()
	
	public static Camera.Size getProperPreviewSize(Camera.Parameters parametrs) {
		List<Camera.Size> sizesList = parametrs.getSupportedPreviewSizes();
		
		Size properSize = sizesList.get(0);
		
		if (sizesList.size() > 1) {
			for (int i = 1; i < sizesList.size(); i++) {
				if (sizesList.get(i).width > properSize.width && sizesList.get(i).height > properSize.height) {
					properSize = sizesList.get(i);
				}
			}
		}
		
		return properSize;
	}
	
	public static String[] getAvaliableVideoSizeStrings(Camera.Parameters parametrs) {
		List<Camera.Size> sizesList = parametrs.getSupportedPreviewSizes();
		String[] list = new String[sizesList.size()];
		
		
		for (int i = 0; i < sizesList.size(); i++) {
			list[i] =  sizesList.get(i).width + "x" + sizesList.get(i).height;
		}
		
		return list;
	}
}


