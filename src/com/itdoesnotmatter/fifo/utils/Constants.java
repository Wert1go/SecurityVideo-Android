package com.itdoesnotmatter.fifo.utils;

import android.content.Context;
import android.media.CamcorderProfile;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public class Constants {
	public static final int CUSTOM_QUALITY = 3223535;
	
	public static final String OPENCELLID_API_KEY = "a54e7ba850e64c5a17eaffd937ef6637";
	
	public static String getUdid(Context context) {
	      WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		    WifiInfo wifiInfo = manager.getConnectionInfo();
		    if (wifiInfo == null || wifiInfo.getMacAddress() == null)
		        return Slipper.md5(Constants.udid);
		    else 
		    	return Slipper.md5(wifiInfo.getMacAddress().replace(":", "").replace(".", ""));
	}
	 
	public static final String udid = Slipper.md5("39" +
	          Build.BOARD + Build.BRAND +
	        	Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
	        	Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
	        	Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
	        	Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
	        	Build.TAGS.length()%10 + Build.TYPE.length()%10 +
	        	Build.USER.length()%10
	        	);
	
	public static String[] videoQualityStrings() {
		String[] string = new String[9];
		
		string[0] = "QUALITY_1080P";
		string[1] = "QUALITY_480P";
		string[2] = "QUALITY_720P";
		string[3] = "QUALITY_CIF";
		string[4] = "QUALITY_HIGH";
		string[5] = "QUALITY_LOW";
		string[6] = "QUALITY_QCIF";
		string[7] = "QUALITY_QVGA";
		string[8] = "QUALITY_CUSTOM";
		return string;
	}
	
	public static int[] videoQualityIds() {
		int[] ids = new int[9];
		
		ids[0] = CamcorderProfile.QUALITY_1080P;
		ids[1] = CamcorderProfile.QUALITY_480P;
		ids[2] = CamcorderProfile.QUALITY_720P;
		ids[3] = CamcorderProfile.QUALITY_CIF;
		ids[4] = CamcorderProfile.QUALITY_HIGH;
		ids[5] = CamcorderProfile.QUALITY_LOW;
		ids[6] = CamcorderProfile.QUALITY_QCIF;
		ids[7] = CamcorderProfile.QUALITY_QVGA;
		ids[8] = CUSTOM_QUALITY;
		return ids;
	}
}
