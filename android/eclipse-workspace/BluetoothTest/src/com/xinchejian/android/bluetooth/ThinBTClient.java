package com.xinchejian.android.bluetooth;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ThinBTClient extends Activity implements SensorEventListener {
	private static final String TAG = ThinBTClient.class.getSimpleName();
	// ==> hardcode your server's MAC arduinoAddress here <==
	private static String arduinoAddress = "00:11:03:14:02:02";
	private static final boolean D = true;
	private static final int UPDATE_TASK_ON_TIMER = 999;
	private RobotControls robotBluetooth;
	private Bluetooth bluetooth;	

		private int x_axis = RobotControls.UPDOWN_DEFAULT_POS;
	private int z_axis = RobotControls.SIDEWAYS_DEFAULT_POS;

	private SeekBar xAxisSeekBar;
	private SeekBar zAxisSeekBar;	
	private TextView currentXAxis;
	private TextView currentZAxis;
	
	// accelerometers test
	private final float[] gravity = new float[3];
	private final Handler handlerTimer = new Handler();
	private final float[] linear_acceleration = new float[3];

	private Sensor mAccelerometer;
	private SensorManager mSensorManager;
	

	private final Runnable taskTimerUpdate = new Runnable() {
		@Override
		public void run() {
			// handling be in the dialog
			// don't mess with GUI from within a thread
			if ((linear_acceleration[0] > 0) && (x_axis < 180)) {
				// x_axis++;
			}

			if ((linear_acceleration[0] < 0) && (x_axis > 0)) {
				// x_axis++;
			}

			// sendMessage(X_AXIS, x_axis);
			// sendMessage(Z_AXIS, z_axis);

			final Message msg = new Message();
			msg.what = UPDATE_TASK_ON_TIMER;
			// handlerEvent.sendMessage(msg);
			handlerTimer.postDelayed(this, 1000);
		}
	};


	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		// TODO Auto-generated method stub

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if (D) {
			Log.e(TAG, "+++ ON CREATE +++");
		}
		bluetooth = new Bluetooth(arduinoAddress);
		if (bluetooth.checkBluetoothAvailable()) {
			Toast.makeText(this, "Bluetooth is not available or is not enabled.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		robotBluetooth = new RobotControls(bluetooth);
		
		
		if (D) {
			Log.e(TAG, "+++ DONE IN ON CREATE, GOT LOCAL BT ADAPTER +++");
		}

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		handlerTimer.removeCallbacks(taskTimerUpdate);
		handlerTimer.postDelayed(taskTimerUpdate, 1000);

		updateViews();
		updateValues();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D) {
			Log.e(TAG, "--- ON DESTROY ---");
			// cleanupBluetooth();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		robotBluetooth.sendControl(RobotControls.Z_AXIS, z_axis);
		robotBluetooth.sendControl(RobotControls.X_AXIS, x_axis);
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		// alpha is calculated as t / (t + dT)
		// with t, the low-pass filter's time-constant
		// and dT, the event delivery rate

		final float alpha = 0.8f;

		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) {
			Log.e(TAG, "++ ON START ++");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D) {
			Log.e(TAG, "-- ON STOP --");
		}
	}

	private void updateValues() {
		currentXAxis.setText("" + x_axis);
		currentZAxis.setText("" + z_axis);
		xAxisSeekBar.setProgress(x_axis);
		zAxisSeekBar.setProgress(z_axis);
	}

	private void updateViews() {
		currentXAxis = (TextView) findViewById(R.id.CurrentXAxis);
		currentZAxis = (TextView) findViewById(R.id.CurrentYAxis);
		xAxisSeekBar = (SeekBar) findViewById(R.id.UpDownSeekBar);
		zAxisSeekBar = (SeekBar) findViewById(R.id.SidewaysSeekBar);
		xAxisSeekBar.setMax(RobotControls.UPDOWN_MAX_POS);
		zAxisSeekBar.setMax(RobotControls.SIDEWAYS_MAX_POS);
		xAxisSeekBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(final SeekBar seekBar,
							final int progress, final boolean fromUser) {
						if (fromUser) {
							if (progress < RobotControls.UPDOWN_MIN_POS) {
								x_axis = RobotControls.UPDOWN_MIN_POS;
							} else {
								x_axis = progress;
							}
						}
						updateValues();
					}

					@Override
					public void onStartTrackingTouch(final SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(final SeekBar seekBar) {
						robotBluetooth.sendControl(RobotControls.X_AXIS, x_axis);
					}
				});
		zAxisSeekBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(final SeekBar seekBar,
							final int progress, final boolean fromUser) {
						if (fromUser) {
							if (progress < RobotControls.SIDEWAYS_MIN_POS) {
								z_axis = RobotControls.SIDEWAYS_MIN_POS;
							} else {
								z_axis = progress;
							}
						}
						updateValues();
					}

					@Override
					public void onStartTrackingTouch(final SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(final SeekBar seekBar) {
						robotBluetooth.sendControl(RobotControls.Z_AXIS, z_axis);
					}
				});
	}
}