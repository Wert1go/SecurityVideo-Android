package com.itdoesnotmatter.fifo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, SurfaceHolder.Callback, PreviewCallback {
	
	MediaRecorder recorder;
    SurfaceHolder holder;
    Button button;
    boolean recording = false;
    File video;
    private Camera mCamera;
    int counter = 0;
    
    Socket socket;
    ParcelFileDescriptor parcelFileDescriptor;
    FileDescriptor fileDescriptor;
    InputStream input;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCamera = Camera.open();
		mCamera.setPreviewCallback(this);
		;
		
		SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);

		this.holder = cameraView.getHolder();
		holder.addCallback(this);
		
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);

        button = (Button) findViewById(R.id.button_start);
        button.setOnClickListener(this);
        
        initData();
	}
	
	private void initData() {
		this.socket = new Socket();
		try {
			this.input = this.socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parcelFileDescriptor = ParcelFileDescriptor.fromSocket(socket);
	}
	
	private void initRecorder() {
		String filePath = Environment.getExternalStorageDirectory() + "/videocapture_example.mp4";
		
		 recorder = new MediaRecorder();
	     mCamera.unlock();
	     recorder.setCamera(mCamera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        
        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
       
        recorder.setOutputFile(filePath);

        //recorder.setOutputFile(this.parcelFileDescriptor.getFileDescriptor());
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(50000000); // Approximately 50 megabytes
        
        prepareRecorder();
    }
	
	private void prepareRecorder() {
        //recorder.setPreviewDisplay(holder.getSurface());
        
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera != null){
            Parameters params = mCamera.getParameters();
            mCamera.setParameters(params);
            try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            mCamera.startPreview();
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (recording) {
            recorder.stop();
            recorder.release();
            recording = false;
        }
		
        releaseCamera();
        finish();
	}

	@Override
	public void onClick(View arg0) {
		if (recording) {
			releaseMediaRecorder();
            recording = false;
        } else {
        	recording = true;
            initRecorder();
            recorder.start();
        }
	}

	protected void stopRecording() {
		recorder.stop();
		recorder.release();
        mCamera.release();
    }

    private void releaseMediaRecorder(){
        if (recorder != null) {
        	recorder.reset();   // clear recorder configuration
            recorder.release(); // release the recorder object
            recorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
        	mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		if (recording)
			Log.e("onPreviewFrame", "count: " + (counter++));
		mCamera.addCallbackBuffer(data);
		
		if(this.socket != null) {
			try {
				Log.e("this.socket != null", "getReceiveBufferSize: " + this.socket.getReceiveBufferSize());
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("onPreviewFrame", "FAIL");
		}
	}
}