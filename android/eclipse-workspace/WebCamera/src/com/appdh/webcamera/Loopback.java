package com.appdh.webcamera;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

public class Loopback {
	private static final String TAG = Loopback.class.getSimpleName();
	//Local data loopback
	private LocalSocket receiver, sender;			
	private LocalServerSocket lss;		
	private final String localAddress;
	private final int bufferSize;
	
	public Loopback(String localAddress, int bufferSize) 
	{
		this.localAddress = localAddress;
		this.bufferSize = bufferSize;
	}
	

	boolean initLoopback()
	{		
		releaseLoopback();

		receiver = new LocalSocket();
		try {
			lss = new LocalServerSocket(localAddress);
			receiver.connect(new LocalSocketAddress(localAddress));
			receiver.setReceiveBufferSize(bufferSize);
			receiver.setSendBufferSize(bufferSize);
		} catch (IOException e) {
			Log.d(TAG, "Couldn't create local interthread connection", e);			
			return false;
		}	
		
		try {
			sender = lss.accept(); // blocks until a new connection arrives!
			sender.setReceiveBufferSize(bufferSize);
			sender.setSendBufferSize(bufferSize);
		} catch (IOException e) {
			Log.d(TAG, "Error waiting for sender!");
			return false;
		}

		return true;
	}


	void releaseLoopback()
	{
		try {
			if ( lss != null){
				lss.close();
			}
			if ( receiver != null){
				receiver.close();
			}
			if ( sender != null){
				sender.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Error closing sockets", e);			
		}
		
		lss = null;
		sender = null;
		receiver = null;
	}
	
	FileDescriptor getTargetFileDescriptor()
	{
		return sender.getFileDescriptor();
	}

	public InputStream getReceiverInputStream() throws IOException {
		return receiver.getInputStream();
	}	
}
