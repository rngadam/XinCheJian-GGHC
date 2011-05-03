package com.xinchejian.android.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


import android.util.Log;

public class RobotControlServer implements Runnable {
	private static final String TAG = RobotControlServer.class.getSimpleName();
	public static final int SERVERPORT = 9090;
	private final RobotControl robotControl;
	private boolean connected = false;
	private boolean isRunning = false;
	private RobotControlServerListener listener;
	
	public RobotControlServer(RobotControl robotControl) {
		this.robotControl = robotControl;
	}
	
	public void setListener(RobotControlServerListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
	        try{
	        	ServerSocket ss = new ServerSocket(SERVERPORT);
	        	setRunning(true);
		        while (isRunning()) {	
		        	Log.d(TAG, "Listening on port " + SERVERPORT);
		            Socket s = ss.accept();
		            Log.d(this.getClass().getSimpleName(), "Accept a Client");
		            setConnected(true);
		            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        Log.d(TAG, "Queueing command: " + line);
                        robotControl.sendCommandString(line);
                        System.out.println("line"); // write to bluetooth
                    }
                    setConnected(false);
		        }
		        
	        } catch (IOException e){
	        	Log.e(TAG , "Could not start listening to socket on port " + SERVERPORT, e);
	        	setRunning(false);
                setConnected(false);
	        	return;
	        } catch(RuntimeException e) {
	        	Log.e(TAG , "Unexpected error in main thread", e);
	        	setRunning(false);
                setConnected(false);
	        	return;        	
	        }
	    }

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean connected) {
		if(connected != this.connected) {
			this.connected = connected;
			if(listener != null) {
				listener.onConnectionStatusChange(connected);
			} else {
				Log.d(TAG, "(no listener) RobotControlServer connected change to: " + connected);
			}
		}
	}
	
	public void setRunning(boolean isRunning) {
		if(isRunning != this.isRunning) {
			this.isRunning = isRunning;
			if(listener != null) {
				listener.onListeningStatusChange(isRunning, SERVERPORT);
			} else {
				Log.d(TAG, "(no listener) RobotControlServer isRunning change to: " + isRunning);
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public interface RobotControlServerListener {
		void onConnectionStatusChange(boolean connected);

		void onListeningStatusChange(boolean isRunning, int serverport);
		
	}
	
}
