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
	private boolean connected;
	
	public RobotControlServer(RobotControl robotControl) {
		this.robotControl = robotControl;
	}
	
	@Override
	public void run() {
	        try{
	        	ServerSocket ss = new ServerSocket(SERVERPORT);
		        while (true) {	
		        	Log.d(TAG, "Listening on port " + SERVERPORT);
		            Socket s = ss.accept();
		            Log.d(this.getClass().getSimpleName(), "Accept a Client");
		            connected = true;
		            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        Log.d(TAG, "Queueing command: " + line);
                        robotControl.sendCommandString(line);
                        System.out.println("line"); // write to bluetooth
                    }
                    connected = false;
		        }
		        
	        } catch (IOException e){
	        	Log.e(TAG , "Could not start listening to socket", e);
	        	return;
	        } catch(RuntimeException e) {
	        	Log.e(TAG , "Unexpected error in main webserver thread", e);
	        	return;        	
	        }
	    }

	public boolean isConnected() {
		return connected;
	}
	
}
