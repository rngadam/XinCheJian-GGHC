package com.xinchejian.android.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

public class ThinBTClient extends Activity implements SensorEventListener  {
	
	private static final String TAG = "THINBTCLIENT";
	private static final boolean D = true;
	private static final byte Z_AXIS = 'S'; // left/right
	private static final byte X_AXIS = 'U'; // up/down
	private static final int UPDATE_STUFF_ON_DIALOG = 999;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private float[] gravity = new float[3];
	private float[] linear_acceleration = new float[3];
	private StringBuilder formatted_output = new StringBuilder();
	private Formatter formatter = new Formatter(formatted_output, Locale.US);
	private Handler handlerTimer = new Handler();
	private BluetoothDevice device;
	
	// Well known SPP UUID (will *probably* map to
	// RFCOMM channel 1 (default) if not in use);
	// see comments in onResume().
	private static final UUID MY_UUID = 
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// ==> hardcode your server's MAC address here <==
	private static String address = "00:11:03:14:02:02";
	private int z_axis = 90;
	private int x_axis = 135;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, 
				"Bluetooth is not available.", 
				Toast.LENGTH_LONG).show(); 
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, 
				"Please enable your BT and re-run this program.", 
				Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (D)
			Log.e(TAG, "+++ DONE IN ON CREATE, GOT LOCAL BT ADAPTER +++");
		
		connectBluetooth();
		
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);		
        handlerTimer.removeCallbacks(taskUpdateStuffOnDialog );
        handlerTimer.postDelayed(taskUpdateStuffOnDialog , 1000); 
        
        updateViews();
        updateValues();
	}
	
	private void updateViews() {
		currentXAxis = (TextView) findViewById(R.id.CurrentXAxis);
		currentZAxis = (TextView) findViewById(R.id.CurrentYAxis);		
		xAxisSeekBar = (SeekBar) findViewById(R.id.UpDownSeekBar);
		zAxisSeekBar = (SeekBar) findViewById(R.id.SidewaysSeekBar);
		xAxisSeekBar.setMax(180);
		zAxisSeekBar.setMax(180);
		xAxisSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
					if(fromUser) {
						x_axis = progress;
						sendMessage(X_AXIS, x_axis);
					}
					updateValues();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});
		zAxisSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
					if(fromUser) {
						z_axis = progress;
						sendMessage(Z_AXIS, z_axis);
					}
					updateValues();					
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});		
	}
	
	private void updateValues() {
		currentXAxis.setText("" + x_axis);
		currentZAxis.setText("" + z_axis);
		xAxisSeekBar.setProgress(x_axis);
		zAxisSeekBar.setProgress(z_axis);
	}
	
	private Runnable taskUpdateStuffOnDialog = new Runnable() {
	       public void run() {      
	            // handling be in the dialog
	            // don't mess with GUI from within a thread
	    	   if(linear_acceleration[0] > 0 && x_axis < 180) {
	    		   //x_axis++;
	    	   }
	    	   
	    	   if(linear_acceleration[0] < 0 && x_axis > 0) {
	    		   //x_axis++;
	    	   }	    	   
	    	   
	    	   //sendMessage(X_AXIS, x_axis);
	    	   //sendMessage(Z_AXIS, z_axis);
	    	   
	            Message msg = new Message();
	            msg.what = UPDATE_STUFF_ON_DIALOG;
	            //handlerEvent.sendMessage(msg);  
	            handlerTimer.postDelayed(this, 1000);
	    }
	};
	private TextView currentXAxis;
	private TextView currentZAxis;
	private SeekBar xAxisSeekBar;
	private SeekBar zAxisSeekBar;

	
	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
	}

	@Override
	public void onResume() {
		super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
		sendMessage(Z_AXIS, z_axis);
		sendMessage(X_AXIS, x_axis);		
	}

	private void connectBluetooth() {
		if (D) {
			Log.e(TAG, "+ ABOUT TO ATTEMPT CLIENT CONNECT +");
		}

		if(device == null) {
			device = mBluetoothAdapter.getRemoteDevice(address);
		}

		// We need two things before we can successfully connect
		// (authentication issues aside): a MAC address, which we
		// already have, and an RFCOMM channel.
		// Because RFCOMM channels (aka ports) are limited in
		// number, Android doesn't allow you to use them directly;
		// instead you request a RFCOMM mapping based on a service
		// ID. In our case, we will use the well-known SPP Service
		// ID. This ID is in UUID (GUID to you Microsofties)
		// format. Given the UUID, Android will handle the
		// mapping for you. Generally, this will return RFCOMM 1,
		// but not always; it depends what other BlueTooth services
		// are in use on your Android device.
		if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
			Log.e(TAG, "Not bonded to " + device.getAddress());
			return;
		}
		
		if(btSocket == null) {
			Log.d(TAG, "Device name: " + device.getName());
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "Socket creation failed.", e);
				return;
			}
	
			// Discovery may be going on, e.g., if you're running a
			// 'scan for devices' search from your handset's Bluetooth
			// settings, so we call cancelDiscovery(). It doesn't hurt
			// to call it, but it might hurt not to... discovery is a
			// heavyweight process; you don't want it in progress when
			// a connection attempt is made.
			mBluetoothAdapter.cancelDiscovery();
	
			// Blocking connect, for a simple client nothing else can
			// happen until a successful connection is made, so we
			// don't care if it blocks.
			try {
				btSocket.connect();
				Log.e(TAG, "BT connection established, data transfer link open.");
			} catch (IOException e) {
				Log.e(TAG, "Could not connect!", e);
				try {
					btSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, 
						"Unable to close socket during connection failure", e2);
				}
				return;
			}	
		}

		if(outStream == null) {
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "Output stream creation failed.", e);
				return;
			}
		}
	}

	private void sendMessage(byte axis, int value) {
		if(outStream == null) {
			connectBluetooth();
			return;
		}
		formatter.format("%1c%03d%d", axis, value, value % 9);
		try {
			byte[] msgBuffer;
			String string = formatted_output.toString();
			Log.d(TAG, "sending: " + string);
			msgBuffer = string.getBytes("ISO-8859-1");
			formatted_output.delete(0, formatted_output.length());
			for(int i=0; i<msgBuffer.length; i++) {
				Log.d(TAG, "value : " + msgBuffer[i]);
				outStream.write(msgBuffer[i]);
			}
			outStream.flush();
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Exception during write.", e);
			outStream = null;
			return;
		} 

	}

	void readMessage() {
		try {
			InputStream inputStream = btSocket.getInputStream();
			while(inputStream.available() != -1) {
				Log.d(TAG, "Read: " + inputStream.read());
			}
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Exception during read.", e);
			return;
		}		
	}
	
	@Override
	public void onPause() {
		super.onPause();

        mSensorManager.unregisterListener(this);		
	}

	private void cleanupBluetooth() {
		if (D)
			Log.e(TAG, "- ON PAUSE -");

		if (outStream != null) {
			try {
				outStream.flush();
				outStream = null;
			} catch (IOException e) {
				Log.e(TAG, "ON PAUSE: Couldn't flush output stream.", e);
			}
		}

		try	{
			btSocket.close();
			btSocket = null;
		} catch (IOException e2) {
			Log.e(TAG, "ON PAUSE: Unable to close socket.", e2);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
		//cleanupBluetooth();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onSensorChanged(SensorEvent event)
     {
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
}