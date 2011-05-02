package com.xinchejian.android.bluetooth;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import android.util.Log;

public class RobotControls {
	private static final String TAG = RobotControls.class.getSimpleName();
	public static final int SIDEWAYS_DEFAULT_POS = 90;
	public static final int SIDEWAYS_MAX_POS = 160;
	public static final int SIDEWAYS_MIN_POS = 20;
	public static final int UPDOWN_DEFAULT_POS = 135;
	public static final int UPDOWN_MAX_POS = 180;
	public static final int UPDOWN_MIN_POS = 55;
	public static final char X_AXIS = 'U'; // up/down
	public static final char Z_AXIS = 'S'; // left/right
	private final Bluetooth bluetooth;
	private final StringBuilder formatted_output = new StringBuilder();
	private final Formatter formatter = new Formatter(formatted_output,
			Locale.US);

	public RobotControls(Bluetooth bluetooth) {
		this.bluetooth = bluetooth;
		this.bluetooth.connect();
	}

	public void sendControl(final char axis, final int value) {
		// examples: U00900, S01800, etc
		formatter.format("%c0%03d0%d", axis, value, value % 9);
		try {
			byte[] msgBuffer;
			final String string = formatted_output.toString();
			Log.d(TAG, "sending: " + string);
			msgBuffer = string.getBytes("ISO-8859-1");
			if (msgBuffer.length != 7) {
				throw new RuntimeException("Unexpected bytes output for: "
						+ string);
			}
			bluetooth.sendBuffer(msgBuffer);
			formatted_output.delete(0, formatted_output.length());
		} catch (final IOException e) {
			Log.e(TAG, "Exception during write.", e);
			return;
		}
	}
}
