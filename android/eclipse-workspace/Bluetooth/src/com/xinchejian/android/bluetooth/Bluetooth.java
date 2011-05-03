package com.xinchejian.android.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Bluetooth {
	private static final String TAG = Bluetooth.class.getSimpleName();

	private final String address;
	// Well known SPP UUID (will *probably* map to
	// RFCOMM channel 1 (default) if not in use);
	// see comments in onResume().
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private BluetoothDevice device;

	public Bluetooth(String address) {
		this.address = address;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	}
	
	public boolean checkBluetoothAvailable() {
		return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
	}
	
	public boolean connect() {
		Log.d(TAG, "+ ABOUT TO ATTEMPT CLIENT CONNECT +");

		if (device == null) {
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
		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			Log.e(TAG, "Not bonded to " + device.getAddress());
			return false;
		}

		if (btSocket == null) {
			Log.d(TAG, "Device name: " + device.getName());
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (final IOException e) {
				Log.e(TAG, "Socket creation failed.", e);
				return false;
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
				Log.e(TAG,
						"BT connection established, data transfer link open.");
			} catch (final IOException e) {
				Log.e(TAG, "Could not connect!", e);
				try {
					btSocket.close();
				} catch (final IOException e2) {
					Log.e(TAG,
							"Unable to close socket during connection failure",
							e2);
				}
				return false;
			}
		}

		if (outStream == null) {
			try {
				outStream = btSocket.getOutputStream();
			} catch (final IOException e) {
				Log.e(TAG, "Output stream creation failed.", e);
				return false;
			}
		}
		return true;
	}

	public synchronized void sendBuffer(byte[] msgBuffer) {
		if (outStream == null) {
			return;
		}	
		for (final byte element : msgBuffer) {
			try {
				outStream.write(element);
				outStream.flush();
			} catch (IOException e) {
				Log.e(TAG, "Error writing to buffer");
				outStream = null;
			}
			// since there's no flow control (CTS/RTS), we need to temper
			// how fast we send things...
			sleep(50);

		}
		
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	void readMessage() {
		try {
			final InputStream inputStream = btSocket.getInputStream();
			while (inputStream.available() != -1) {
				Log.d(TAG, "Read: " + inputStream.read());
			}
		} catch (final IOException e) {
			Log.e(TAG, "ON RESUME: Exception during read.", e);
			return;
		}
	}

	public boolean isConnected() {
		return outStream != null;
	}	
}
