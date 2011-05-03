package com.appdh.webcamera;

import java.io.FileDescriptor;
import java.io.IOException;
import android.util.Log;
import android.media.MediaRecorder;

/*
 * 使用方法：
 * 1. prepareMedia()
 * 2.    setupOutput()
 * 3.	 
 */

public class MediaSource {

	private final static String TAG = MediaSource.class.getSimpleName();

	//Camera 数据对象
	private MediaRecorder mRecorder;	
	private CameraView	mViewer;	
		
	//状态控制
	private boolean bInited = false;
	private boolean bPrepared = false;	
	private boolean bStreaming = false;
	
	public MediaSource(CameraView showView)
	{
		mViewer = showView;	
		bInited = false;
		bPrepared = false;
		bStreaming = false;
	}
	public boolean isInited()
	{
		return bInited;
	}
	public boolean isStreaming()
	{
		return bStreaming;
	}
	public boolean isPrepared()
	{
		return bPrepared;
	}
	public void prepareOutput(FileDescriptor targetFile)
	{
		mRecorder.setOutputFile(targetFile);
		try {
			mRecorder.prepare();
			bPrepared = true;
			return;
		} catch (IllegalStateException e) {
			Log.e(TAG, "Could not prepare output to " + targetFile, e);		
		} catch (IOException e) {
			Log.e(TAG, "Could not prepare output to " + targetFile, e);		
		}
		
		releaseMedia();		
	}
	public void prepareOutput(String targetFile)
	{	
		mRecorder.setOutputFile(targetFile);
		
		try {
			mRecorder.prepare();
			bPrepared = true;
			return;
		} catch (IllegalStateException e) {
			Log.e(TAG, "Could not prepare output to " + targetFile, e);		
		} catch (IOException e) {
			Log.e(TAG, "Could not prepare output to " + targetFile, e);	
		}
		
		releaseMedia();		
	}
	
	public void startCapture()
	{
		if(!bInited) {
			Log.e(TAG, "Cannot start capture, not initialized!");
			return;
		}
		
		mRecorder.start();
		
		bStreaming = true;
	}
	
	public void stopCapture()
	{
		if(mRecorder != null) {
			mRecorder.stop();
			releaseMedia();
		}
	}
	
	public boolean initMedia()
	{					
		bInited = false;
		initCamera();		
		
		bStreaming = false;
		bPrepared = false;	
		return bInited;
	}

	public void releaseMedia()
	{
		if ( mRecorder != null)
		{
			mRecorder.reset();
			mRecorder.release();	
			mRecorder = null;
		}
		
		bInited = false;
		bPrepared = false;
		bStreaming = false;
	}
	
	private void initCamera()
	{						
		if ( mRecorder != null){
			mRecorder.reset();
			mRecorder.release();
		}
		mRecorder = new MediaRecorder();
		try {
	    	mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);  
	    	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	        mRecorder.setVideoFrameRate(27);
	        mRecorder.setVideoSize(640, 480);   
		} catch(RuntimeException e) {
			Log.e(TAG, "Could not set camera settings",e );
			return;
		}
        
        if(mViewer.hasSurface)
		{			
			mRecorder.setPreviewDisplay(mViewer.holder.getSurface());
		} else {
			Log.e(TAG, "No surface available to set as preview");
			return;
		}
        bInited = true;
        Log.d(TAG,"init Camera setting is OK! ");
	}
}
