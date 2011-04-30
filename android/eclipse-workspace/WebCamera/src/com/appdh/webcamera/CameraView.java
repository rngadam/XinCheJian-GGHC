package com.appdh.webcamera;


import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback
{
	//var
	public SurfaceHolder holder;
    public boolean hasSurface = false;       
    
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
    
    //SurfaceView ʵ�ֺ���  surfaceCreated, surfaceDestroyed, surfaceChanged
    public void surfaceCreated(SurfaceHolder holder) 
    {
    	hasSurface = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) 
    {      
    	hasSurface = false;    	
    }

    //��Viewer���F�ĕr���@ʾPreviewӳ��
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
    {
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
