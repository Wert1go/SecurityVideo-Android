package com.itdoesnotmatter.fifo;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.itdoesnotmatter.fifo.model.VideoFile;
import com.itdoesnotmatter.fifo.model.VideoQueue;
import com.itdoesnotmatter.fifo.utils.CameraUtils;
import com.itdoesnotmatter.fifo.utils.IntentKeys;

public class CameraActivity extends Activity{
	private static final String TAG = "CameraActivity";
	
	public static final int SERVER_PORT = 4444;
	private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording = false;

    private MediaRecorder mMediaRecorder;
    private Button captureButton;
    private VideoQueue videoQueue;
    
    private int mRecorderMode = -1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_preview_layout);
        
        Bundle extra = this.getIntent().getExtras();
        
        mRecorderMode = extra.getInt(VideoManager.RECORD_MODE, -1);
        
        Log.e("mRecorderMode", mRecorderMode + "");
        
        // Create an instance of Camera
        mCamera = CameraUtils.getCameraInstance();
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	captureButton.setSelected(!captureButton.isSelected());
                	if (isRecording) {
                		new ConvertTask().execute();
                	}
                	toggleRecord();
                }
            }
        );

    }
    
    public void toggleRecord() {
    	if (isRecording) {
    		stopRecording();
        } else {
        	// initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                // inform the user that recording has started
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }
    
    private boolean prepareVideoRecorder(){
    	
    	if (mCamera == null) {
    		mCamera = CameraUtils.getCameraInstance();
    	}
    	
    	if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            
            // Step 1: Unlock and set camera to MediaRecorder
    	}
    	
    	mCamera.unlock();
    	
    	mMediaRecorder.setCamera(mCamera);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        profile.videoBitRate = VideoManager.DEFAULT_BITRATE;
        profile.videoFrameRate = VideoManager.DEFAULT_FPS;
        mMediaRecorder.setProfile(profile);
        
        VideoFile videoFile = new VideoFile();
        videoFile.setName(targetString());
        this.getVideoQueue().pushVideoFile(videoFile);
        // Step 4: Set output file
        mMediaRecorder.setOutputFile(videoFile.getFilePath());
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        int maxLenght = 0;
        if (this.mRecorderMode == VideoManager.MODE_VIDEO_REG) {
        	maxLenght = VideoManager.MAX_VIDEO_LEN;
        } else {
        	maxLenght = VideoManager.MAX_VIDEO_LEN_REPORT;
        }
        
        mMediaRecorder.setMaxDuration(maxLenght);
        // Step 6: Prepare configured MediaRecorder
        
        mMediaRecorder.setOnInfoListener(new OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				// TODO Auto-generated method stub
				if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
					if (mRecorderMode == VideoManager.MODE_VIDEO_REG) {
						toggleRecord();
					} else {
						if (isRecording) {
							stopRecording();
							captureButton.setSelected(!captureButton.isSelected());
						}
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
        long timestamp = System.currentTimeMillis();
        
        String targetString = timestamp + ".mp4";
        
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
//            mMediaRecorder.reset();   // clear recorder configuration
//            mMediaRecorder.release(); // release the recorder object
//            mMediaRecorder = null;
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
    
    public VideoQueue getVideoQueue() {
    	if (videoQueue == null) {
    		videoQueue = new VideoQueue();
    	}
		return videoQueue;
	}

	public void setVideoQueue(VideoQueue videoQueue) {
		this.videoQueue = videoQueue;
	}

	class RecordTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			
			return null;
		}
		
    }
	
	class ConvertTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			if (isRecording) {
               stopRecording();
            }
			
			VideoManager manager = new VideoManager(getBaseContext());
            manager.createVideoFromQueue(getVideoQueue(), mRecorderMode);
			return null;
		}
    }
	
	public void stopRecording() {
		 // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object

        // inform the user that recording has stopped
        isRecording = false;
	}
}
