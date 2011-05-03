package com.xinchejian.android.robot;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appdh.webcamera.CameraView;
import com.appdh.webcamera.StreamingServer;
import com.xinchejian.android.bluetooth.Bluetooth;
import com.xinchejian.android.bluetooth.RobotControl;
import com.xinchejian.android.bluetooth.RobotControlServer;

public class RobotServerActivity extends Activity {
	// ==> hardcode your server's MAC arduinoAddress here <==
	private static String arduinoAddress = "00:11:03:14:02:02";
	private static final String TAG = RobotServerActivity.class.getSimpleName();
	private Bluetooth bluetooth;	
	private boolean bluetoothToastShown = false;
	private FaceView faceView;
	private final Handler handlerTimer = new Handler();
	private RobotControl robotControl;
	private RobotControlServer robotControlServer;
	private RobotControlServerView robotControlServerView;
	
	private Thread serverThread;
	private StreamingServer streamingServer;
	private final Runnable taskTimerUpdate = new Runnable() {
		private boolean triedStartingStreaming = false;

		@Override
		public void run() {
			if (bluetooth.checkBluetoothAvailable()) {
				if(!bluetoothToastShown) {
					toast("Bluetooth is not available or is not enabled.");
					bluetoothToastShown = true;
				}
				robotControlServerView.setBluetoothStatus(false);
			} else if (!bluetooth.isConnected()){
				robotControlServerView.setBluetoothStatus(bluetooth.connect());
			}
			robotControlServerView.setNetworkStatus(robotControlServer.isConnected());
			
			if(!streamingServer.isReadyToStream() && !triedStartingStreaming) {
				if(cameraView.hasSurface) {
					triedStartingStreaming = true;
					streamingServer.setCameraView(cameraView);
					streamingServer.startDetectCamera();
					// see ya in onSampleVideoIsValid()!
				} else {
					Log.w(TAG, "CameraView has no surface yet, can't prepare streaming");
				}
			}
			// check back later
			handlerTimer.postDelayed(this, 1000);
		}
	};

	StreamingServer.StreamingServerEventHandler streamingHandler = new StreamingServer.StreamingServerEventHandler() {
		@Override
		public void onSampleVideoIsInvalid() {
			toast("Sample video is invalid");
		}

		@Override
		public void onSampleVideoIsValid() {
			toast("Sample video is valid, preparing streaming");	
			streamingServer.prepareStreaming();
			// see you in onStreamingServerReady
		}

		@Override
		public void onStreamingStarted() {
			toast("Streaming started");						
		}

		@Override
		public void onVideoDetectionStarted() {
			toast("Video detection started");					
		}

		@Override
		public void onWebServerStarted(String url) {
			toast("Webserver started and listening at " + url);	
			
		}

		@Override
		public void onStreamingServerReady() {
			toast("Streaming ready, starting WebServer");
			streamingServer.startWebServer();		
			// see you in onWebServerStarted!
		}

		@Override
		public void onCaptureFailed(Exception e) {
			toast("Failed capturing video: " + e.getMessage());
			Log.e(TAG, "Exception message ", e);
		}
		
	};
	private CameraView cameraView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout main = new LinearLayout(this);
		setContentView(main);
		robotControlServerView = new RobotControlServerView(this);
		//main.addView(robotControlServerView);

		faceView = new FaceView(this);
		faceView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		main.addView(faceView);
		
		cameraView = new CameraView(this);
		main.addView(cameraView);
		
		bluetooth = new Bluetooth(arduinoAddress);
		robotControl = new RobotControl(bluetooth);		
		
		robotControlServer = new RobotControlServer(robotControl);
		serverThread = new Thread(robotControlServer);
		serverThread.start();
		
		streamingServer = new StreamingServer("/sdcard/robot", streamingHandler);
		
		handlerTimer.postDelayed(taskTimerUpdate, 1000);
	}

	private void toast(String msg) {
		Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG).show();
		Log.d(TAG, msg);
	}
}