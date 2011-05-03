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

public class StreamingServer implements StreamingHandler, Runnable {
	public interface StreamingServerEventHandler {
		public abstract void onCaptureFailed(Exception e);
		public abstract void onSampleVideoIsInvalid();
		public abstract void onSampleVideoIsValid();
		public abstract void onStreamingServerReady();
		public abstract void onStreamingStarted();
		public abstract void onStreamingStopped();
		public abstract void onVideoDetectionStarted();
		public abstract void onWebServerFailed();
		public abstract void onWebServerStarted(String url);
		public abstract void onStreamingFailed();
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
				setReadyToCapture(true);
				eventHandler.onSampleVideoIsValid();
			} else {
				// notify video setup is wrong
				setReadyToCapture(false);
				eventHandler.onSampleVideoIsInvalid();
			}
		}
	};	
	private final StreamingServerEventHandler eventHandler;
	private boolean isReadyToCapture = false;
	private boolean isReadyToStream = false;
	private boolean isStreaming = false;
	private boolean webServerIsRunning = false;
	
	private WebServer.WebServerListener listener = new WebServer.WebServerListener() {

		@Override
		public void onRunningStatusChange(boolean isRunning) {
			if(isRunning != isReadyToServe()) {
				setWebServerIsRunning(isRunning);
				if(isRunning) {
					if(WebServer.getInterfaces() != null) {
						url = "http:/" + WebServer.getInterfaces() + ":" + WebServer.SERVERPORT;
						eventHandler.onWebServerStarted(url);
						setReadyToStream(true);
					} else {
						mWebServer.stop();
						eventHandler.onWebServerFailed();
						Log.e(TAG, "No interface found");
					}		
				} else {
					if(isStreaming()) {
						stopStreaming();
					}
					eventHandler.onWebServerFailed();
				}
			}
		}
	};
	private Loopback loopback = new Loopback("appdh.com", StreamingKernel.TYPICAL_FRAME_SIZE);
	private MediaSource mMediaSource;
	private Streamer mStreamer;
	private StreamingKernel mStreamingKernel;
	private Thread	  mStreamingKernelThread;
	private WebServer mWebServer;
	private Thread	  mWebServerThread;

	private Timer rePrepareStreamTimer;
	private String url;
	private CameraView mCameraView;
	
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
    	if(isStreaming()) {
    		throw new IllegalStateException("Already streaming (last connection did not get cleaned up?");    		
    	}
    	setStreaming(true);
		Log.d(TAG, "Starting capture and streaming");

		mMediaSource.startCapture();
		mStreamer.doStreaming(outputStream);
		stopStreaming();    	
		rePrepareStreaming();    
	}
	
	public boolean isReadyToCapture() {
		return mMediaSource != null && isReadyToCapture;
	}
	
	public boolean isReadyToStream() {
    	return isReadyToCapture() && isReadyToStream;
    }
    
    public boolean isStreaming() {
		return isStreaming;
	}   

    public void prepareStreaming()
    {
    	if(!isReadyToCapture()) {
    		throw new IllegalStateException("Not ready to capture,can't start StreamingKernel (startDetectCamera first)");
    	}    	
    	if(isReadyToStream()) {
    		throw new IllegalStateException("Already ready to stream");
    	}
		if(mStreamingKernel == null)
			mStreamingKernel = new StreamingKernel(loopback, 60, false);		
		if(mStreamer == null)
			mStreamer = new Streamer(mStreamingKernel);
		
		mStreamingKernel.repareStreaming();
		
		Log.d(TAG, "Initing loopback");
		if(!loopback.initLoopback()) {
			Log.e(TAG, "Could not initialize loopback");
			return;	
		}
		
		Log.d(TAG, "Initing media source and media source output");
		mMediaSource.initMedia();
		mMediaSource.prepareOutput(loopback.getTargetFileDescriptor());			
		
		Log.d(TAG, "Spawning Streaming Kernel thread");
		mStreamingKernelThread = new Thread( mStreamingKernel );			
		mStreamingKernelThread.start();


		setReadyToStream(true);
    }
    
    public void setCameraView(CameraView view) {
    	mCameraView = view; 
		mMediaSource = new MediaSource(view);	 
	}

	public void setReadyToCapture(boolean isReadyToCapture) {
		this.isReadyToCapture = isReadyToCapture;
	}
	
	public void setReadyToStream(boolean isReadyToStream) {
		if(isReadyToStream != this.isReadyToStream) {
			this.isReadyToStream = isReadyToStream;
			if(isReadyToStream)
				eventHandler.onStreamingServerReady();
		}
	}

	public void setStreaming(boolean isStreaming) {
		this.isStreaming = isStreaming;
		// notify client received and starting capture
		// todo: spawn different thread here?
		if(isStreaming) {
			eventHandler.onStreamingStarted();
		} else {
			eventHandler.onStreamingStopped();			
		}
	}

	public boolean startDetectCamera()
	{
		if(mMediaSource == null) {
			throw new IllegalStateException("setCameraView should be called first");
		}
		// notify video detection started
		setReadyToCapture(false);    	
		eventHandler.onVideoDetectionStarted();

		TimerTask startDetectCameraTask = new TimerTask() {
			@Override
			public void run() {
				try {
					if(!mMediaSource.initMedia()) {
						Log.e(TAG, "Could not init MediaSource!");
					}
					mMediaSource.prepareOutput(autodetection_mp4);	
					Log.d(TAG, "Starting test capture");
					mMediaSource.startCapture();
				} catch(RuntimeException e) {
					mMediaSource.releaseMedia();	
					eventHandler.onCaptureFailed(e);
					Log.e(TAG, "Failed starting capture: ", e);
				}
			}
		};

		
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
		detectCameraTimer.schedule(startDetectCameraTask, 0);		
		detectCameraTimer.schedule(stopDetectCameraTask, 3000);	

		return true;
	}

	public void startWebServer()
    {    	
    	if(!isReadyToCapture()) {
    		throw new IllegalStateException("Not ready to capture, can't start WebServer (startDetectCamera first)");
    	}
		//Æô¶¯ Web·þÎñ¶ÔÏó
		mWebServer = new WebServer(this);		
		mWebServer.setWebServerListener(listener);
		mWebServerThread = new Thread( mWebServer );
		mWebServerThread.start();
    }

	public void stopStreaming()
    {
		setReadyToStream(false);
		if(mWebServer != null)
			if(mWebServer.isRunning())
				mWebServer.stop();
    	if(mStreamingKernel != null)
    		mStreamingKernel.stopStreaming();
    	if(mMediaSource != null)
    		mMediaSource.isStreaming();
    			mMediaSource.stopCapture();
    	setStreaming(false);
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

	public void setWebServerIsRunning(boolean webServerIsRunning) {
		this.webServerIsRunning = webServerIsRunning;
	}

	public boolean isReadyToServe() {
		return webServerIsRunning;
	}
	enum states { WAIT_SURFACE, DETECT_CAMERA, WAIT_DETECT, WAIT_STREAMING, WAIT_WEBSERVER, WAIT_CONNECTION, STREAMING}; 
	states state = states.WAIT_SURFACE;
	
	@Override
	public void run() {
		while(true) {
			switch(state) {
			case WAIT_SURFACE:
				if(mMediaSource != null && mCameraView.hasSurface)
					state = states.DETECT_CAMERA;
				break;
			case DETECT_CAMERA:
				startDetectCamera();
				state = states.WAIT_DETECT;
				break;
			case WAIT_DETECT:
				if(isReadyToCapture()) {
					prepareStreaming();
					state = states.WAIT_STREAMING;
				}
				break;
			case WAIT_STREAMING:
				if(isReadyToStream()) {
					startWebServer();	
					state = states.WAIT_WEBSERVER;
				}
				break;
			case WAIT_WEBSERVER:
				if(isReadyToServe()) {
					state = states.WAIT_CONNECTION;	
				}
				break;
			case WAIT_CONNECTION:
				if(isStreaming()) {
					state = states.STREAMING;
				} 
				break;
			case STREAMING:
				return;
			}
		}
	}
}
