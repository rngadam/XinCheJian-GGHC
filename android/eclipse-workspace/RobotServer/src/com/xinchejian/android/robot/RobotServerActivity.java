package com.xinchejian.android.robot;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
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
	private CameraView cameraView;
	private final RobotControlServer.RobotControlServerListener controlServerListener = new  RobotControlServer.RobotControlServerListener() {

		@Override
		public void onConnectionStatusChange(boolean connected) {
			if(connected)
				toast("RobotControlServer connected to client");
			else
				toast("RobotControlServer not connected to client");
		}

		@Override
		public void onListeningStatusChange(boolean isRunning, int serverport) {
			if(isRunning)
				toast("RobotControlServer listening on port " + serverport);
			else
				toast("RobotControlServer stopped listening on port " + serverport);	
		}
		
	};
	private FaceView faceView;
	private final Handler handlerTimer = new Handler();
	private String lastMessage;
	
	private RobotControl robotControl;
	private RobotControlServer robotControlServer;
	private RobotControlServerView robotControlServerView;
	private Thread serverThread;

	private final StreamingServer.StreamingServerEventHandler streamingHandler = new StreamingServer.StreamingServerEventHandler() {
		@Override
		public void onCaptureFailed(Exception e) {
			toast("Failed capturing video: " + e.getMessage());
			Log.e(TAG, "Exception message for video capture failure ", e);
		}

		@Override
		public void onSampleVideoIsInvalid() {
			toast("Sample video is invalid");
		}

		@Override
		public void onSampleVideoIsValid() {
			toast("Sample video is valid");	

		}

		@Override
		public void onStreamingServerReady() {
			toast("Streaming ready");
		}

		@Override
		public void onStreamingStarted() {
			toast("Streaming started");		
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					faceView.setVisibility(SurfaceView.VISIBLE);
					cameraView.setVisibility(SurfaceView.INVISIBLE);
				}
			});
		}

		@Override
		public void onStreamingStopped() {
			toast("Streaming stopped");		
		}

		@Override
		public void onVideoDetectionStarted() {
			toast("Video detection started");					
		}

		@Override
		public void onWebServerFailed() {
			toast("Failed starting video streaming webserver!");
		}

		@Override
		public void onWebServerStarted(String url) {
			streamingWebServerUrl = url;
			toast("Webserver started and listening at " + streamingWebServerUrl);	
			
		}

		@Override
		public void onStreamingFailed() {
			toast("Streaming failed");	
		}
		
	};
	
	private StreamingServer streamingServer;

	
	private final Runnable taskTimerUpdate = new Runnable() {
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
			if(streamingServer.isReadyToStream() && !streamingServer.isStreaming()) {
				toast("Please connect to " + streamingWebServerUrl);
			}
			// check back later
			handlerTimer.postDelayed(this, 300);
		}
	};
	protected String streamingWebServerUrl;
	private Thread streamingServerThread;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// View
		LinearLayout main = new LinearLayout(this);
		setContentView(main);
		robotControlServerView = new RobotControlServerView(this);
		//main.addView(robotControlServerView);

		faceView = new FaceView(this);
		faceView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		main.addView(faceView);
		faceView.setVisibility(SurfaceView.INVISIBLE);
		
		cameraView = new CameraView(this);
		cameraView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		main.addView(cameraView);
		cameraView.setVisibility(SurfaceView.VISIBLE);

		// Servers
		bluetooth = new Bluetooth(arduinoAddress);
		robotControl = new RobotControl(bluetooth);		
		
		robotControlServer = new RobotControlServer(robotControl);
		robotControlServer.setListener(controlServerListener);
		serverThread = new Thread(robotControlServer);
		serverThread.start();
		
		streamingServer = new StreamingServer("/sdcard/robot/autodetection_mp4", streamingHandler);
		streamingServer.setCameraView(cameraView);
		streamingServerThread = new Thread(streamingServer);
		streamingServerThread.start();
		
		handlerTimer.postDelayed(taskTimerUpdate, 300);
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handlerTimer.removeCallbacks(taskTimerUpdate);
		streamingServer.stopStreaming();
		robotControlServer.setRunning(false);
	}
	
	private synchronized void toast(final String msg) {
		if(lastMessage == null || msg.compareTo(lastMessage) != 0) {
			Log.d(TAG, msg);
			lastMessage = msg;
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_LONG).show();
				}
			});
		}

	}

}