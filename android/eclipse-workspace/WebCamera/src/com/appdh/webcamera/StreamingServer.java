package com.appdh.webcamera;

/****
 * Hiding the camera:
 * http://soledadpenades.com/2011/04/07/how-to-hide-the-camera-preview-in-android/
 * 

Before starting the camera preview…

Set the camera’s preview display to the preview’s SurfaceHolder
Set the preview surface to VISIBLE

And after starting the camera preview…

Set the preview surface to GONE or INVISIBLE
 */
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class StreamingServer implements StreamingHandler {
	public interface StreamingServerEventHandler {
		public abstract void onSampleVideoIsInvalid();
		public abstract void onSampleVideoIsValid();
		public abstract void onStreamingStarted();
		public abstract void onVideoDetectionStarted();
		public abstract void onWebServerStarted(String url);
		public abstract void onStreamingServerReady();
		public abstract void onCaptureFailed(Exception e);
	}
	
	private final static String TAG = StreamingServer.class.getSimpleName();	
	private final String autodetection_mp4;		
	private Handler beginProcessCameraHandler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) {        	
        	// notify video started
    		
    		Thread processThread = new Thread()
    		{
				@Override
    			public void run(){
    	    		if(!Streamer.detectSetuped(autodetection_mp4)) {
    	    			Log.e(TAG, "Could not complete auto-detection");
    	    		}    	    		
    	    		endProcessCameraHandler.sendEmptyMessage(0);
    	    	}
    	    };
    	    processThread.start();
         }
	};		
	private Timer detectCameraTimer;				
	private Handler endProcessCameraHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {        	
			if(Streamer.isValid()){
				// notify video setup OK
				eventHandler.onSampleVideoIsValid();
			} else {
				// notify video setup is wrong
				eventHandler.onSampleVideoIsInvalid();
			}
		}
	};	
	private final StreamingServerEventHandler eventHandler;
	private Loopback loopback = new Loopback("appdh.com", StreamingKernel.TYPICAL_FRAME_SIZE);
	private MediaSource mMediaSource;
	private Streamer mStreamer;
	private StreamingKernel mStreamingKernel;
	private Thread	  mStreamingKernelThread;
	private WebServer mWebServer;
	private Thread	  mWebServerThread;
	private Timer rePrepareStreamTimer;
	private boolean isReadyToStream = false;
	private boolean isReadyToCapture = false;

	public StreamingServer(final String autodetection_mp4, final StreamingServerEventHandler eventHandler) {
		this.autodetection_mp4 = autodetection_mp4;
		this.eventHandler = eventHandler;
	}	

    /* (non-Javadoc)
	 * @see com.appdh.webcamera.StreamingHandler#doStreaming(java.io.OutputStream)
	 */
	@Override
	public void doStreaming(OutputStream outputStream) {
    	if(!isReadyToStream()) {
    		throw new IllegalStateException("Not ready to capture or stream (should only be called by WebServer)");
    	}		
		Log.d(TAG, "Starting capture and streaming");
		// notify client received and starting capture
		eventHandler.onStreamingStarted();
		mMediaSource.startCapture();
		mStreamer.doStreaming(outputStream);
		stopStreaming();    	
		rePrepareStreaming();    
	}
	
	public void setCameraView(CameraView view) {
		mMediaSource = new MediaSource(view);	 
	}
	
	public void startDetectCamera()
	{
		if(mMediaSource == null) {
			throw new IllegalStateException("setCameraView should be called first");
		}
		// notify video detection started
		isReadyToCapture = false;    	
		eventHandler.onVideoDetectionStarted();

		try {
			if(!mMediaSource.initMedia()) {
				Log.e(TAG, "Could not init MediaSource!");
				return;
			}
			mMediaSource.prepareOutput(autodetection_mp4);		
			mMediaSource.startCapture();
		} catch(RuntimeException e) {
			mMediaSource.releaseMedia();	
			eventHandler.onCaptureFailed(e);
			return;
		}
		TimerTask stopDetectCameraTask = new TimerTask()
	    {
			@Override
			public void run()
	    	{
				try {
					mMediaSource.stopCapture();
				} catch(RuntimeException e) {
					Log.e(TAG, "MediaSource.stopCapture called when not started!");
				}
				mMediaSource.releaseMedia();	
	    		beginProcessCameraHandler.sendEmptyMessage(0);
	    	}
	    };
	    
		//TimerÈÎÎñ
		if(detectCameraTimer == null ){						
			detectCameraTimer = new Timer();
		}		
		detectCameraTimer.schedule(stopDetectCameraTask, 3000);	
		isReadyToCapture = true;
	}
    
    public void startWebServer()
    {    	
    	if(!isReadyToCapture) {
    		throw new IllegalStateException("Not ready to capture, can't start WebServer (startDetectCamera first)");
    	}
		//Æô¶¯ Web·þÎñ¶ÔÏó
		mWebServer = new WebServer(this);		
		mWebServerThread = new Thread( mWebServer );
		mWebServerThread.start();
		
		// notify WebServer listening to above address
		String url = "http:/" + WebServer.getInterfaces() + ":8080";
		eventHandler.onWebServerStarted(url);
		isReadyToStream = true;
    }   

    public boolean isReadyToStream() {
    	return isReadyToCapture && isReadyToStream;
    }
    
    public void prepareStreaming()
    {
    	if(!isReadyToCapture) {
    		throw new IllegalStateException("Not ready to capture,can't start StreamingKernel (startDetectCamera first)");
    	}    	
		if(mStreamingKernel == null)
			mStreamingKernel = new StreamingKernel(loopback, 60, false);		
		if(mStreamer == null)
			mStreamer = new Streamer(mStreamingKernel);
		
		mStreamingKernel.repareStreaming();
		
		Log.d(TAG, "Initing media source and media source output");
		mMediaSource.initMedia();			
		mMediaSource.prepareOutput(loopback.getTargetFileDescriptor());			
		
		Log.d(TAG, "Spawning Streaming Kernel thread");
		mStreamingKernelThread = new Thread( mStreamingKernel );			
		mStreamingKernelThread.start();
		
		eventHandler.onStreamingServerReady();
    }

	private void rePrepareStreaming()
    {
    	TimerTask rePrepareStreamingTask = new TimerTask()
	    {
	    	@Override
			public void run()
	    	{
	    		prepareStreaming();
	    	}
	    };
	    
    	//TimerÈÎÎñ
		if(rePrepareStreamTimer == null ){						
			rePrepareStreamTimer = new Timer();
		}		
		rePrepareStreamTimer.schedule(rePrepareStreamingTask, 1000);	
    }
	
	public void stopStreaming()
    {
		isReadyToStream = false;
		mWebServer.stop();
    	mStreamingKernel.stopStreaming();
    	mMediaSource.stopCapture();
    }
}
