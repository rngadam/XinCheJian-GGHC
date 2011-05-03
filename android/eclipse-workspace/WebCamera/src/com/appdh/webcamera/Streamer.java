package com.appdh.webcamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;

import android.util.Log;

public class Streamer {
	private static final String TAG = Streamer.class.getSimpleName();
	private int underflow = 0;
	private BufferedOutputStream targetOS;
	private static AvcConfigurationBox avcConfigurationBox;
	private StreamingKernel kernel;
	private long startTime;
	private long totalFramesSent;
	
	public Streamer(StreamingKernel kernel) {
		this.kernel = kernel;
	}
	
	public void doStreaming(OutputStream os)
	{   
		startTime = System.currentTimeMillis();
		
		targetOS = new BufferedOutputStream(os, StreamingKernel.MAX_FRAME_SIZE); 
		byte[] sps = avcConfigurationBox.getSequenceParameterSets().get(0);
		byte[] pps = avcConfigurationBox.getPictureParameterSets().get(0);
		
		Log.d(TAG, "Building video header");
		try {			    	    		
	    	Log.v(TAG, "Writing FlvHeader ");
	    	MediaPackage.writeFlvHeader(targetOS);
	    	Log.v(TAG, "Writing VideoHeader with sps (SequenceParameterSets) and PictureParameterSets");
			MediaPackage.writeVideoHeader(targetOS, sps,  pps);
			targetOS.flush();    					
		} catch (IOException e) {     
			Log.e(TAG, "Failed to build video header", e);
			return;
		}   
		
		byte[] tempBuffer;		
		int vflag;
		long ts;
		int tempSize;
		
		sleep(1000);
		
		Log.d(TAG, "Streaming loop");
		while(true){
			tempBuffer = kernel.getReadBuffer();
			if ( tempBuffer != null){
				vflag = kernel.getVideoFlag();
				ts = kernel.getTimeStamp();
				tempSize = kernel.getReadLength();
				
				try {
					Log.v(TAG, "Writing to target(s) " + tempSize + " bytes");
	    			MediaPackage.writeFlvPackage(targetOS, tempBuffer, tempSize, ts, vflag);
				} catch (IOException e) {
					Log.d(TAG, "Failed to write to output streams", e);			
					break;
				}
				
				kernel.releaseRead();
				try {
					targetOS.flush();
				} catch (IOException e) {
					Log.d(TAG, "Failed to flush output stream", e);			
					break;
				}
				totalFramesSent++;
					
			} else {
				underflow++;
				sleep(10);
			}
		}
		
		try {
			targetOS.close();
		} catch (IOException e) {
			Log.d(TAG, "Failed to close to the target OS", e);		
		}	
	}

	   
    void sleep(int duration_ms) {
		try {
			Thread.sleep(duration_ms);
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
	}
    
	public static boolean detectSetuped(String autodetection_filename)
	{
        IsoBufferWrapper isoBufferWrapper;
		try {
			isoBufferWrapper = new IsoBufferWrapper(new File(autodetection_filename));
		} catch (RuntimeException e) {
			Log.e(TAG, "Could not open file " + autodetection_filename, e);			
			return false;
		} catch (IOException e) {
			Log.e(TAG, "Could not open file " + autodetection_filename, e);			
			return false;
		}
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        try {
			isoFile.parse();
		} catch (RuntimeException e) {
			Log.e(TAG, "Could not parse file " + autodetection_filename, e);			
			return false;
		} catch (IOException e) {
			Log.e(TAG, "Could not parse file " + autodetection_filename, e);			
			return false;
		}
        
        avcConfigurationBox = (AvcConfigurationBox) IsoFileConvenienceHelper.get(isoFile, "moov/trak/mdia/minf/stbl/stsd/avc1/avcC");
		if(avcConfigurationBox == null) {
			Log.e(TAG, "AVC configuration file not found in test file " + autodetection_filename);
			return false;
		}
        if(avcConfigurationBox.getPictureParameterSets().size() != 1) {
			Log.e(TAG, "Multiple PictureParameterSets! " + avcConfigurationBox.getPictureParameterSets().size());
			return false;
		}
		if(avcConfigurationBox.getSequenceParameterSets().size() != 1) {
			Log.e(TAG, "Multiple SequenceParameterSets! " + avcConfigurationBox.getSequenceParameterSets().size());
			return false;
		}    	
		byte[] sps = avcConfigurationBox.getSequenceParameterSets().get(0);
		for(int i=0; i<sps.length; i++) {
			Log.d(TAG, "sps[" + i + "] = " + sps[i]);
		}
		byte[] pss = avcConfigurationBox.getPictureParameterSets().get(0);
		for(int i=0; i<pss.length; i++) {
			Log.d(TAG, "pss[" + i + "] = " + pss[i]);
		}		
		return true;
	}
	
	public static boolean isValid() {
		return avcConfigurationBox != null;
	}

	public int getFramesPerSecond() {
		long elapsedSeconds = (System.currentTimeMillis() - startTime)/1000L;
		return (int) (totalFramesSent/(elapsedSeconds+1));
	}

}
