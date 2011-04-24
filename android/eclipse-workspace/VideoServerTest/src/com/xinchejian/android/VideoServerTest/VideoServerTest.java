package com.xinchejian.android.VideoServerTest;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoServerTest extends Activity implements SurfaceHolder.Callback {
	private static final int port = 9999;
	private Camcorder camcorder;
	private String TAG = VideoServerTest.class.getSimpleName();

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
		camcorder.startRecordingToDisk("/sdcard/test.mp4", surfaceHolder);        
	}
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		try {
			camcorder = new Camcorder(port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		camcorder.stopRecording();
	}
    
}