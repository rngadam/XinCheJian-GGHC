package com.xinchejian.android.VideoServerTest;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoServerTest extends Activity implements SurfaceHolder.Callback {
	private static final int port = 9999;
	private Camcorder camcorder;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(camcorder != null) {
			camcorder.stopRecording();
		}
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
    }
    
	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int arg1, int arg2, int arg3) {
		camcorder.setSurfaceHolder(surfaceHolder);
		if(camcorder.isConnected()) {
			camcorder.startRecording();
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		try {
			camcorder = new Camcorder(port);
		} catch (UnknownHostException e) {
			Log.e(this.getClass().getSimpleName(), "Invalid host", e);
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), "Could not start camcorder", e);
		}		
		camcorder.setSurfaceHolder(surfaceHolder);
		camcorder.startRecordingToNetwork();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		camcorder.stopRecording();
		camcorder.setSurfaceHolder(null);
	}
    
}