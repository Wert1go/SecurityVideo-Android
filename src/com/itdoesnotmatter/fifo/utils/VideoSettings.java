package com.itdoesnotmatter.fifo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;

public class VideoSettings {
	public static final String VideoSettings = "VideoSettings";
	
	public static final String audioBitRate = "audioBitRate";
	public static final String audioChannels = "audioChannels";
	public static final String audioCodec = "audioCodec";
	public static final String audioSampleRate = "audioSampleRate";
	public static final String duration = "duration";
	public static final String fileFormat = "fileFormat";
	public static final String quality = "quality";
	public static final String videoBitRate = "videoBitRate";
	public static final String videoCodec = "videoCodec";
	public static final String videoFrameHeight = "videoFrameHeight";
	public static final String videoFrameRate = "videoFrameRate";
	public static final String videoFrameWidth = "videoFrameWidth";

	public static void saveLastSettings(CamcorderProfile profile, Context context) {
		SharedPreferences settings = context.getSharedPreferences(VideoSettings, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putInt(audioBitRate, profile.audioBitRate);
	      editor.putInt(audioChannels, profile.audioChannels);
	      editor.putInt(audioCodec, profile.audioCodec);
	      editor.putInt(audioSampleRate, profile.audioSampleRate);
	      editor.putInt(duration, profile.duration);
	      editor.putInt(fileFormat, profile.fileFormat);
	      editor.putInt(quality, profile.quality);
	      editor.putInt(videoCodec, profile.videoCodec);
	      editor.putInt(videoFrameHeight, profile.videoFrameHeight);
	      editor.putInt(videoFrameRate, profile.videoFrameRate);
	      editor.putInt(videoFrameWidth, profile.videoFrameWidth);
	      editor.putInt(videoBitRate, profile.videoBitRate);
	      editor.commit();
	}
	
	public static CamcorderProfile getLastSettings(Context context) {
		CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
		
		SharedPreferences settings = context.getSharedPreferences(VideoSettings, 0);
	    profile.audioBitRate = settings.getInt(audioBitRate, 0);
	    profile.audioChannels = settings.getInt(audioChannels, 0);
	    profile.audioCodec = settings.getInt(audioCodec, 0);
	    profile.audioSampleRate = settings.getInt(audioSampleRate, 0);
	    profile.duration = settings.getInt(duration, 0);
	    profile.fileFormat = settings.getInt(fileFormat, 0);
	    profile.quality = settings.getInt(quality, 0);
	    profile.videoCodec = settings.getInt(videoCodec, 0);
	    profile.videoFrameHeight = settings.getInt(videoFrameHeight, 0);
	    profile.videoFrameRate = settings.getInt(videoFrameRate, 0);
	    profile.videoFrameWidth = settings.getInt(videoFrameWidth, 0);
	    profile.videoBitRate = settings.getInt(videoBitRate, 0);
		return profile;
	}
}
