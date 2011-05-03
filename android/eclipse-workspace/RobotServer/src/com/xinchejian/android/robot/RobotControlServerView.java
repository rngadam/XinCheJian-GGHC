package com.xinchejian.android.robot;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class RobotControlServerView extends LinearLayout {

	//private OnControlUpdateListener listener;
	private ToggleButton network;
	private ToggleButton bluetooth;
	
	public RobotControlServerView(Context context) {
		super(context);
		inflate(context, R.layout.server, this);
		
		network = (ToggleButton) findViewById(R.id.toggleButton1);
		bluetooth = (ToggleButton) findViewById(R.id.toggleButton2);
		network.setTextOff("NET-OFF");
		network.setTextOn("NET-ON");
		bluetooth.setTextOff("BT-OFF");
		bluetooth.setTextOn("BT-ON");	
		setNetworkStatus(false);
		setBluetoothStatus(false);
	}
	
	public void setNetworkStatus(boolean status) {
		network.setChecked(status);
	}

	public void setBluetoothStatus(boolean status) {
		bluetooth.setChecked(status);
	}
	/*
	private void networkStatusChange() {
		if(listener != null)
			listener.networkStatusChange(network.get);
	}
	
	private void bluetoothStatusChange() {
		if(listener != null)
			listener.onSidewaysUpdate(sideways_pos);	
	}
	
	public void setOnControlUpdatelistener(OnControlUpdateListener listener) {
		this.listener = listener;
	}
	
	public interface OnControlUpdateListener {
		 public abstract void networkStatusChange(boolean status);
		 public abstract void bluetoothStatusChange(int x);
	} 
	*/
}
