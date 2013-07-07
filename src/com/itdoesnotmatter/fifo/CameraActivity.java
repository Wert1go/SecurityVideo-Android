package com.itdoesnotmatter.fifo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.itdoesnotmatter.fifo.utils.CameraUtils;
import com.itdoesnotmatter.fifo.utils.Constants;
import com.itdoesnotmatter.fifo.utils.IntentKeys;
import com.itdoesnotmatter.fifo.utils.SocketUtils;
import com.itdoesnotmatter.fifo.utils.VideoSettings;

public class CameraActivity extends Activity{
	private static final String TAG = "CameraActivity";
	
	private static final int MAX_DURATION = 5 * 60 * 1000;
	
	public static final int SERVER_PORT = 4444;
	private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording = false;
    private String qualityString;
    private int qualityCode;
    private boolean customRecord;
    private MediaRecorder mMediaRecorder;


    ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out;
    
    public String targetFilePath;
    
    FileDescriptor fileDescriptor;
    
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
        
        new ServerSocketTask().execute();
        
        
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
                        if (clientSocket != null) {
                     		try {
								clientSocket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                     		Log.d(TAG, "Socket: Closed.");
                     	}
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
        
        
    }
    
    private class ServerSocketTask extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			createTcpServer();
			return null;
		}
    	
		private void createTcpServer() {
			try {
				serverSocket = new ServerSocket(SERVER_PORT);
				publishProgress("Слушаем порт: " + SERVER_PORT);
				Timer myTimer = new Timer(); // Создаем таймер
				
				myTimer.schedule(new TimerTask() { // Определяем задачу
				    @Override
				    public void run() {
				    	 createAnotherClient();
				    }
				}, 1L * 1000); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.
				while (true) {
					Socket client = serverSocket.accept();

					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					if (client != null)
						publishProgress("Соединение установленно");
					
					try {	
						InputStream  in = client.getInputStream();
	                    PrintWriter outStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client
	                            .getOutputStream())), true);
	                    int line = 0;
	                    byte data[] = new byte[1024];
	                    while ((line = in.read(data)) != -1) {
	                        Log.d(TAG, "" + line);
	                        
	                        //outStream.println(line);
	                    }
	                    
	                    Log.d(TAG, "C: createAnotherClient!!!");
	                    break;
	                } catch (Exception e) {
	                	publishProgress("Oops. Connection interrupted. Please reconnect your phones.");
	                    e.printStackTrace();
	                }
					
					out.println("hello bitches!");
				}
			} catch (IOException e) {
				publishProgress("Ошибка");
				Log.e(TAG, e.getLocalizedMessage());
			}
	    }
	}

    
    public void createAnotherClient() {
		try {
			Log.d(TAG, "C: createAnotherClient");
            InetAddress serverAddr = InetAddress.getByName(SocketUtils.getIPAddress(true));
            Log.d(TAG, "C: Connecting...");
            clientSocket = new Socket(serverAddr, SERVER_PORT);
            Log.d(TAG, "C: Connected");
            try {
            	BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket
                            .getOutputStream())), true);
                out.println("Hey Server!");
                
                String line = null;
                while ((line = in.readLine()) != null) {
                    Log.d(TAG, "Server say: " + line);
                    
                }
            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
            }
           
        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        }
	}

    public FileDescriptor getDescriptor() {
		if (this.clientSocket == null) {
			Log.e(TAG, "SOCKET ERROR");
			return null;
		}

		return ParcelFileDescriptor.fromSocket(this.clientSocket).getFileDescriptor();
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
        this.fileDescriptor = getDescriptor();
        if (this.fileDescriptor != null) {
        	Log.e(TAG, "WRITE TO SOCKET");
        	mMediaRecorder.setOutputFile(this.fileDescriptor);
        } else {
        	Log.e(TAG, "WRITE TO FILE");
        	mMediaRecorder.setOutputFile(targetString());
        }
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setMaxDuration(MAX_DURATION);
        // Step 6: Prepare configured MediaRecorder
        
        mMediaRecorder.setOnInfoListener(new OnInfoListener() {

			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				// TODO Auto-generated method stub
				if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
        	
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
        
        String targetString = Environment.getExternalStorageDirectory() + "/video_" + this.qualityString + "_" + profile.videoFrameWidth + "x" + profile.videoFrameHeight + "_br" + profile.videoBitRate + "_fps" + profile.videoFrameRate + ".mp4";
        
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
    	 try {
         	if (clientSocket != null) {
         		clientSocket.close();
         		Log.d(TAG, "Socket: Closed.");
         	}
         	if (serverSocket != null) {
         		serverSocket.close();
         		Log.d(TAG, "Server: Shutdown.");
         	}
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
