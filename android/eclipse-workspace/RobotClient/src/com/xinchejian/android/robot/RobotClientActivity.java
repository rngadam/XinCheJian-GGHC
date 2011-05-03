package com.xinchejian.android.robot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.xinchejian.android.bluetooth.RobotControl;
import com.xinchejian.android.bluetooth.RobotControlClient;

public class RobotClientActivity extends Activity {
	private static final String TAG = RobotClientActivity.class.getSimpleName();
	private RobotControlClient client;
	private RobotControlClientView robotControlsView;
	private AnimationControlView animationControlView;
	private RobotControlClient robotControlClient;
	private Thread clientThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		robotControlClient = new RobotControlClient("127.0.0.1");
		clientThread = new Thread(robotControlClient);
		clientThread.start();
		
		LinearLayout main = new LinearLayout(this);
		main.setOrientation(LinearLayout.VERTICAL);
		setContentView(main);
		
		robotControlsView = new RobotControlClientView(this);
		robotControlsView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		main.addView(robotControlsView);
		
		animationControlView = new AnimationControlView(this);
		animationControlView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		main.addView(animationControlView, 320, 240);
		
		robotControlsView.setOnControlUpdatelistener(new RobotControlClientView.OnControlUpdateListener() {
			@Override
			public void onUpDownUpdate(int x) {
				if(client != null)
					client.sendControl(RobotControl.UPDOWN_AXIS, x);
				else 
					Log.w(TAG, "UpDown update received but we are not connected!");
			}
			
			@Override
			public void onSidewaysUpdate(int x) {
				if(client != null)
					client.sendControl(RobotControl.SIDEWAYS_AXIS, x);				
				else 
					Log.w(TAG, "Sideways update received but we are not connected!");
			}
		});
	}
}