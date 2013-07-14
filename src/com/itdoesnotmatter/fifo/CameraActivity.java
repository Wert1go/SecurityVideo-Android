package com.itdoesnotmatter.fifo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.itdoesnotmatter.fifo.utils.CameraUtils;
import com.itdoesnotmatter.fifo.utils.Constants;
import com.itdoesnotmatter.fifo.utils.IntentKeys;
import com.itdoesnotmatter.fifo.utils.VideoSettings;

public class CameraActivity extends Activity{
	private static final String TAG = "CameraActivity";
	
	private static final int MAX_DURATION = 20 * 1000;
	
	public static final int SERVER_PORT = 4444;
	private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording = false;
    private String qualityString;
    private int qualityCode;
    private boolean customRecord;
    private MediaRecorder mMediaRecorder;
    
    public String targetFilePath;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_preview_layout);
        
        Bundle extra = this.getIntent().getExtras();
        
        int id = extra.getInt(IntentKeys.VIDEO_QUALITY, -1);
        
        if (this.qualityCode != -1) {
        	this.qualityCode = Constants.videoQualityIds()[id];
        	this.qualityString = Constants.videoQualityStrings()[id];
        } else {
        	this.qualityCode = Constants.CUSTOM_QUALITY;
        	this.qualityString = Constants.videoQualityStrings()[8];
        }
        
        if (this.qualityCode == Constants.CUSTOM_QUALITY) {
        	this.customRecord = true;
        }
        
        // Create an instance of Camera
        mCamera = CameraUtils.getCameraInstance();
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        this.targetFilePath = targetString();
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRecording) {
                        // stop recording and release camera
                        mMediaRecorder.stop();  // stop the recording
                        releaseMediaRecorder(); // release the MediaRecorder object
                        mCamera.lock();         // take camera access back from MediaRecorder
  
                        // inform the user that recording has stopped
                        isRecording = false;
                        
                        File file = new File(targetFilePath);
                        long length = file.length();
                        length = length/1024;
                        
                        Toast toast = Toast.makeText(getBaseContext(), "Resut file size: " + length + " KB", Toast.LENGTH_LONG);
                        toast.show();
                        
                    } else {
                        // initialize video camera
                        if (prepareVideoRecorder()) {
                            // Camera is available and unlocked, MediaRecorder is prepared,
                            // now you can start recording
                            mMediaRecorder.start();
                            
                            Log.e("mMediaRecorder.start()", "mMediaRecorder.start()");
                            
                            // inform the user that recording has started
                            isRecording = true;
                        } else {
                            // prepare didn't work, release the camera
                            releaseMediaRecorder();
                            // inform user
                        }
                    }
                }
            }
        );
        
//        VideoManager videoManager = new VideoManager();
//        videoManager.cut();
    }
 
    private boolean prepareVideoRecorder(){
    	
    	if (mCamera == null) {
    		mCamera = CameraUtils.getCameraInstance();
    	}
    	
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile profile  = null;
        if (!this.customRecord) {
        	profile = CamcorderProfile.get(this.qualityCode);
        } else {
        	profile = VideoSettings.getLastSettings(this.getBaseContext());
        }
        
        mMediaRecorder.setProfile(profile);
        // Step 4: Set output file
        mMediaRecorder.setOutputFile(targetString());
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setMaxDuration(MAX_DURATION);
        // Step 6: Prepare configured MediaRecorder
        
        mMediaRecorder.setOnInfoListener(new OnInfoListener() {

			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				
				Log.e("onInfo", "what " + what);
				
				// TODO Auto-generated method stub
				if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
					mr.stop();
					releaseMediaRecorder();
					isRecording = false;
					
					if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();
                        
                        Log.e("mMediaRecorder.start()", "mMediaRecorder.start()");
                        
                        // inform the user that recording has started
                        isRecording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
				}
			}
        	
        });
        
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    
    private String targetString() {
    	CamcorderProfile profile  = null;
        if (!this.customRecord) {
        	profile = CamcorderProfile.get(this.qualityCode);
        } else {
        	profile = VideoSettings.getLastSettings(this.getBaseContext());
        }
        
        long timestamp = System.currentTimeMillis();
        
//        String targetString = Environment.getExternalStorageDirectory() + "/Video/video_" + this.qualityString + "_" + 
//        profile.videoFrameWidth + "x" + profile.videoFrameHeight + "_br" + profile.videoBitRate + "_fps" + 
//        		profile.videoFrameRate + ".mp4";
        
        String targetString = Environment.getExternalStorageDirectory() + "/Video/" + timestamp + ".mp4";
        
        Log.e("test", targetString);
        
        return targetString;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
           this.setRecordInProgress(false);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (mCamera == null) {
    		mCamera = CameraUtils.getCameraInstance();
    	}
    	this.mPreview.setCamera(mCamera);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();

    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    private void setRecordInProgress(boolean state) {
    	this.isRecording = state;
    }
}
