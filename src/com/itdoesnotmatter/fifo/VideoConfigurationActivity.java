package com.itdoesnotmatter.fifo;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.itdoesnotmatter.fifo.utils.CameraUtils;
import com.itdoesnotmatter.fifo.utils.IntentKeys;
import com.itdoesnotmatter.fifo.utils.VideoSettings;

public class VideoConfigurationActivity extends Activity implements OnClickListener{
	final String[] codecTitles = new String[] {"Default", "H263", "H264", "MPEG4"};
	final String[] fileFormatTitles = new String[] {"Default", "THREE_GPP", "MPEG_4"};
	List<Camera.Size> sizesList;
	String[] sizeStrings;
	Button videoCodec;
	Button fileFormat;
	Button videoSizes;
	
	Button done;

	EditText duration;
	EditText frameRate;
	EditText width;
	EditText height;
	EditText bitRate;
	
	CamcorderProfile profile;
	
	Camera mCamera;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_settings_layout);
		
		if (this.mCamera == null) {
			mCamera = CameraUtils.getCameraInstance();
		}
		
		Camera.Parameters parameters = mCamera.getParameters();
		sizesList = parameters.getSupportedPreviewSizes();
		sizeStrings = CameraUtils.getAvaliableVideoSizeStrings(parameters);
		
		profile = VideoSettings.getLastSettings(getBaseContext());
		
		videoCodec = (Button) findViewById(R.id.video_codec_picker);
		fileFormat = (Button) findViewById(R.id.file_format_picker);
		done = (Button) findViewById(R.id.done_button);
		videoSizes = (Button) findViewById(R.id.video_size);
		videoCodec.setOnClickListener(this);
		fileFormat.setOnClickListener(this);
		done.setOnClickListener(this);
		videoSizes.setOnClickListener(this);
		
		duration = (EditText) findViewById(R.id.duration_field);
		frameRate = (EditText) findViewById(R.id.frame_rate_field);
		//width = (EditText) findViewById(R.id.frame_width_field);
		//height = (EditText) findViewById(R.id.frame_height_field);
		bitRate = (EditText) findViewById(R.id.video_bit_rate_field);
		restoreData();
	}
	
	public void onPause() {
		super.onPause();
		this.releaseCamera();
	}
	
	public void onResume() {
		super.onResume();
		if (this.mCamera == null) {
			mCamera = CameraUtils.getCameraInstance();
		}
	}
	
	public void restoreData() {
		duration.setText(profile.duration + "");
		frameRate.setText(profile.videoFrameRate + "");
		//width.setText(profile.videoFrameWidth + "");
		//height.setText(profile.videoFrameHeight + "");
		bitRate.setText(profile.videoBitRate + "");
		videoSizes.setText(profile.videoFrameWidth + "x" + profile.videoFrameHeight);
		videoCodec.setText(codecTitles[profile.videoCodec]);
		fileFormat.setText(fileFormatTitles[profile.fileFormat]);
	}
	
	public void collectData() {
		profile.duration = Integer.parseInt(duration.getEditableText().toString());
		profile.videoFrameRate = Integer.parseInt(frameRate.getEditableText().toString());
		//profile.videoFrameWidth = Integer.parseInt(width.getEditableText().toString());
		//profile.videoFrameHeight = Integer.parseInt(height.getEditableText().toString());
		profile.videoBitRate = Integer.parseInt(bitRate.getEditableText().toString());
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.video_codec_picker:

			final int[] codecs = new int[] {
					MediaRecorder.VideoEncoder.DEFAULT, 
					MediaRecorder.VideoEncoder.H263, 
					MediaRecorder.VideoEncoder.H264,
					MediaRecorder.VideoEncoder.MPEG_4_SP
					};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Video codecs");
			builder.setItems(codecTitles, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {			    	
			    	profile.videoCodec = codecs[item];
			    	videoCodec.setText(codecTitles[item]);
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();

			break;
		case R.id.file_format_picker:
			
			final int[] fileFormats = new int[] {
					MediaRecorder.OutputFormat.DEFAULT,
					MediaRecorder.OutputFormat.THREE_GPP,
					MediaRecorder.OutputFormat.MPEG_4
					
					};
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("File formats");
			builder1.setItems(fileFormatTitles, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {			    	
			    	profile.fileFormat = fileFormats[item];
			    	fileFormat.setText(fileFormatTitles[item]);
			    }
			});
			AlertDialog alert1 = builder1.create();
			alert1.show();
			break;
		case R.id.done_button:
			Intent intent2 = new Intent (this, CameraActivity.class);
			intent2.putExtra(IntentKeys.VIDEO_QUALITY, 8);
			collectData();
			VideoSettings.saveLastSettings(profile, getBaseContext());
			this.startActivity(intent2);
			break;
			
		case R.id.video_size:
				
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle("Video sizes");
				builder2.setItems(sizeStrings, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {			    	
				    	profile.videoFrameWidth = sizesList.get(item).width;
				    	profile.videoFrameHeight = sizesList.get(item).height;
				    	videoSizes.setText(sizeStrings[item]);
				    }
				});
				AlertDialog alert2 = builder2.create();
				alert2.show();
				break;
		}
	}
	
	private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}