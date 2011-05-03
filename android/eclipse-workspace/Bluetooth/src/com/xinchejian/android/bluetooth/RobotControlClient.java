package com.xinchejian.android.bluetooth;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class RobotControlClient implements Runnable {
	private static final String TAG = RobotControlClient.class.getSimpleName();
	private String serverAddress;
	private boolean connected;
	private List<String> commands;
	private Socket socket;
	private PrintWriter out;
	private final StringBuilder formatted_output = new StringBuilder();	
	private final Formatter formatter = new Formatter(formatted_output,
			Locale.US);
	private int waitDelay;	
	public RobotControlClient(String serverAddress) {
		super();
		this.serverAddress = serverAddress;
		connected = false;
	}
	
	public void queueCommand(String s) {
		commands.add(s);
	}

	@Override
	public void run() {
		// keep reconnecting
		while(true) {
			try {
				InetAddress serverAddr = InetAddress.getByName(serverAddress);
				socket = new Socket(serverAddr, RobotControlServer.SERVERPORT);
		        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
		                .getOutputStream())), true);
		        connected = true;
			} catch (UnknownHostException e) {
				Log.e(TAG, "Unknown host: Could not find " + serverAddress + ":" + RobotControlServer.SERVERPORT);
			} catch (IOException e) {
				Log.e(TAG, "IOException: Could not connect to " + serverAddress + ":" + RobotControlServer.SERVERPORT);	
			}
	
	        while (connected) {
	            try {
	                Log.d("ClientActivity", "C: Sending command.");
	
	                 // where you issue the commands
	                 if(!commands.isEmpty()) {
	                	String command = commands.remove(0);
						out.write(command + '\n');
						Log.d(TAG, "Sent  " + command);
	                 }
	                 sleep(200);
	            } catch (Exception e) {
	                Log.e(TAG, "S: Error", e);
	            }
	        }
	        if(socket != null) {
		        try {
					socket.close();
				} catch (IOException e) {
		            Log.e(TAG, "Error closing the socket", e);
				}
	        }
			else {
				waitDelay += 1000;
				sleep(waitDelay);
			}
		}
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendControl(final char axis, final int value) {
		queueCommand(getCommandString(axis, value));
	}
	

	public String getCommandString(final char axis, final int value) {
		synchronized(formatter) {
			formatter.format("%c0%03d0%d", axis, value, value % 9);
			final String string = formatted_output.toString();
			formatted_output.delete(0, formatted_output.length());
			return string;
		}
	}	
}
