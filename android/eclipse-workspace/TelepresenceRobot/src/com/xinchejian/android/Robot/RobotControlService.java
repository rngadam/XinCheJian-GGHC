package com.xinchejian.android.Robot;

import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class RobotControlService extends Service {

	private WifiManager wifiManager;
	private String[][] accessPoints = {
			{ "\"LightArtStudio\"", "\"andy315315\"" },
			{ "\"rngadam\"", "helloworld" }, 
			{ "\"MLHAP\"", "bonjour!" }, 
	};
	
	private String TAG = RobotControlService.class.getSimpleName();
	private Handler mHandler = new Handler();

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (!wifiManager.setWifiEnabled(true)) {
				Log.e(TAG, "Cannot enable wifi network!");
			}
			int i;
			for (i = 0; i < accessPoints.length; i++) {
				int netId = getNetId(wifiManager, accessPoints[i][0],
						accessPoints[i][1]);
				if (wifiManager.enableNetwork(netId, true)) {
					break;
				}
			}
			if (i == accessPoints.length) {
				Log.e(TAG, "No connection yet, retrying in 1s");
				mHandler.postDelayed(mUpdateTimeTask, 1000);
			}

		}
	};

	public void initWifiTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}

	public void onCreate() {
		Log.v(RobotControlService.class.getSimpleName(), "Created");
		wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		initWifiTimer();

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		mBluetoothAdapter.enable();
	}

	private int getNetId(WifiManager wifiManager, String ssid, String password) {
		List<WifiConfiguration> configuredNetworks = wifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration wc : configuredNetworks) {
			if (wc.SSID.equals(ssid)) {
				Log.d(RobotControlService.class.getSimpleName(),
						"Already configured: " + ssid);
				return wc.networkId;
			}
		}
		Log.d(RobotControlService.class.getSimpleName(), "Creating wifi: "
				+ ssid);
		WifiConfiguration config1 = new WifiConfiguration();
		config1.SSID = ssid;
		config1.preSharedKey = password;
		return wifiManager.addNetwork(config1);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
