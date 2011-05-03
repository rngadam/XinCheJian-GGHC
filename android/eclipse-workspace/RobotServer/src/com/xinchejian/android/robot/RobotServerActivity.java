package com.xinchejian.android.robot;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xinchejian.android.bluetooth.Bluetooth;
import com.xinchejian.android.bluetooth.RobotControl;
import com.xinchejian.android.bluetooth.RobotControlServer;

public class RobotServerActivity extends Activity {
	private static final String TAG = RobotServerActivity.class.getSimpleName();
	// ==> hardcode your server's MAC arduinoAddress here <==
	private static String arduinoAddress = "00:11:03:14:02:02";
	private Bluetooth bluetooth;	
	private RobotControl robotControl;
	private final Handler handlerTimer = new Handler();
	private Thread serverThread;
	private RobotControlServerView robotControlServerView;
	private boolean bluetoothToastShown = false;
	private final Runnable taskTimerUpdate = new Runnable() {
		@Override
		public void run() {
			if (bluetooth.checkBluetoothAvailable()) {
				if(!bluetoothToastShown) {
					Toast.makeText(getApplicationContext(), "Bluetooth is not available or is not enabled.",
							Toast.LENGTH_LONG).show();
					bluetoothToastShown = true;
				}
				robotControlServerView.setBluetoothStatus(false);
			} else if (!bluetooth.isConnected()){
				robotControlServerView.setBluetoothStatus(bluetooth.connect());
			}
			robotControlServerView.setNetworkStatus(robotControlServer.isConnected());
			// check back later
			handlerTimer.postDelayed(this, 1000);
		}
	};
	private RobotControlServer robotControlServer;
	private FaceView faceView;

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
		
		bluetooth = new Bluetooth(arduinoAddress);
		robotControl = new RobotControl(bluetooth);		
		
		robotControlServer = new RobotControlServer(robotControl);
		serverThread = new Thread(robotControlServer);
		serverThread.start();

		handlerTimer.postDelayed(taskTimerUpdate, 1000);
	}
}