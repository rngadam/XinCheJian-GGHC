package com.xinchejian.android.bluetooth;

import java.io.IOException;

import android.util.Log;

public class RobotControl {
	private static final String TAG = RobotControl.class.getSimpleName();
	public static final int SIDEWAYS_DEFAULT_POS = 90;
	public static final int SIDEWAYS_MAX_POS = 160;
	public static final int SIDEWAYS_MIN_POS = 20;
	public static final int UPDOWN_DEFAULT_POS = 135;
	public static final int UPDOWN_MAX_POS = 180;
	public static final int UPDOWN_MIN_POS = 55;
	public static final char UPDOWN_AXIS = 'U'; // up/down
	public static final char SIDEWAYS_AXIS = 'S'; // left/right
	
	private final Bluetooth bluetooth;

	public RobotControl(Bluetooth bluetooth) {
		this.bluetooth = bluetooth;
	}

	public void sendCommandString(final String string) {

		try {
			byte[] msgBuffer;
			Log.d(TAG, "sending: " + string);
			msgBuffer = string.getBytes("ISO-8859-1");
			if (msgBuffer.length != 7) {
				throw new RuntimeException("Unexpected bytes output for: "
						+ string);
			}
			synchronized(bluetooth) {
				bluetooth.sendBuffer(msgBuffer);
			}
		} catch (final IOException e) {
			Log.e(TAG, "Exception during write.", e);
			return;
		}
	}

}
