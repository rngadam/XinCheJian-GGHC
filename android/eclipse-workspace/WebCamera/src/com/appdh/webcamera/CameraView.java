package com.appdh.webcamera;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback
{
	private final static String TAG = CameraView.class.getSimpleName();
	//var
	public SurfaceHolder holder;
    public boolean hasSurface = false;       
    
    public CameraView(Context context) 
    {
        super(context);
        initSurface();            
    }    
    
    public CameraView(Context context, AttributeSet attrs) 
    {
        super(context,attrs);
        initSurface();            
    }
    
    private void initSurface()
    {
    	holder = getHolder();
    	holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	holder.addCallback(this);      	
    }
    
    //SurfaceView 实现函数  surfaceCreated, surfaceDestroyed, surfaceChanged
    public void surfaceCreated(SurfaceHolder holder) 
    {
    	Log.d(TAG, "Surface created");
    	hasSurface = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) 
    {      
    	Log.d(TAG, "Surface destroyed");
    	hasSurface = false;    	
    }

    //當Viewer出現的時候，顯示Preview映像
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
    {
    	Log.d(TAG, "Surface changed");
    	this.holder = holder;
    	/*
    	if ( hasSurface && streamer != null){
    		streamer.setSurface(holder.getSurface());
    	} else {
    		if ( streamer != null) {
    			streamer.setSurface(null);
    		}
    	}
    	*/
    }
}
