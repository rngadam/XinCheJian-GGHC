package com.appdh.webcamera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.SystemClock;
import android.util.Log;


public class StreamingKernel implements Runnable
{
	private static final String SDCARD_WEBCAMERA_TEMP_MP4 = "/sdcard/webcamera/StreamingKernel.mp4";
	private static final String TAG = StreamingKernel.class.getSimpleName();
	private long frameDuration;

	//internal video buffer and frame timing
	static VideoBuffer videoBuffer = null;
	static TimeStampEstimator frameTimeStamp = null;
	public static final int MAX_FRAME_SIZE = 32384;
	public static final int TYPICAL_FRAME_SIZE = 4096;
	private BufferedOutputStream localCopy;
	private InputStream fis;
	private boolean localCopyNeeded;
	private int overflow = 0;
	private long startTime;
	private long bytesRead;
	private final Loopback loopback;
	
	public int getOverflow() {
		return overflow;
	}

	public StreamingKernel(Loopback loopback, long duration, boolean localCopyNeeded)	
	{
		this.loopback = loopback;
		frameDuration = duration;
		this.localCopyNeeded = localCopyNeeded;
		
		if ( videoBuffer == null )
			//videoBuffer = new VideoBuffer(128, MAX_FRAME_SIZE);			
			videoBuffer = new VideoBuffer(16, MAX_FRAME_SIZE);			
		if ( frameTimeStamp == null)
			frameTimeStamp = new TimeStampEstimator(frameDuration);
	}
	
	
	public void repareStreaming()
	{
		videoBuffer.reset();
		frameTimeStamp.reset(frameDuration);
	}

	public void stopStreaming()
	{
		loopback.releaseLoopback();
	}
	public int getVideoFlag()
	{
		return videoBuffer.getVideoFlag();
	}
	public long getTimeStamp()
	{
		return videoBuffer.getTimeStamp();
	}
	public byte[] getReadBuffer()
	{
		return videoBuffer.getReadBuffer();
	}
	public int getReadLength()
	{
		return videoBuffer.getReadLength();
	}
	public void releaseRead()
	{
		videoBuffer.releaseRead();
	}

	public void run()
	{

		realRun();	
		Log.e(TAG, "Streaming aborted due to loop return, closing");
		loopback.releaseLoopback();
		Log.d(TAG, "Flushing and closing target FLV file");
		if(localCopy != null) {
			try {
				localCopy.flush();
				localCopy.close();
			} catch (IOException e) {
				Log.e(TAG, "Error closing flv file");
			}
		}
	}


	private void realRun() {
		int dlen;

		Log.d(TAG, "Create local video byte stream object"); 
		try {
			fis = loopback.getReceiverInputStream();
		} catch (IOException e) {
			Log.e(TAG, "STREAMING FAILED: Error creating raw MP4 input stream", e);
			return;
		}   
		
		if(localCopyNeeded) {
			try {
				localCopy = new BufferedOutputStream(new FileOutputStream(SDCARD_WEBCAMERA_TEMP_MP4));
			} catch (IOException e) {
				Log.e(TAG, "STREAMING FAILED: Error creating FLV output stream", e);
				return;
			}   
		}

		Log.d(TAG, "Waiting to skip MP4 header offset");
		try {
			Log.d(TAG, "Found after reading " + MediaDetect.checkMP4_MDAT(fis) + " bytes");
		} catch (IOException e) {
			Log.e(TAG, "Could not find header!",e );
			return;
		}
		Log.d(TAG, "First frame duration computing");
		
		frameTimeStamp.setFirstFrameTiming();
		startTime = System.currentTimeMillis();
		while(true){
			if(!videoBuffer.isEmptySpace()) {
				overflow++;
				sleep(10);
				continue;
			}
			
			byte[] buffer = videoBuffer.nextAvailableBuffer();
			dlen = fillBuffer(buffer, 0, 4, fis);
			if(dlen != 4 ){
				Log.d(TAG, "Reader Package's Header error!");
				break;
			}

			int package_size = (buffer[1]&0xFF)*65536 + (buffer[2]&0xFF)*256 + (buffer[3]&0xFF);

			dlen = fillBuffer(buffer, 4, package_size, fis);
			if(dlen != package_size){
				Log.e(TAG, "Reader Package's data error dlen = " + dlen);
				break;
			}

			Log.v(TAG, "Read package data of length " + dlen + " to queue in video buffer ring");
			Log.v(TAG, "Empty video buffer, adding package of size " + dlen);
			videoBuffer.updateBuffer(package_size+4, frameTimeStamp.getSequenceTimeStamp(), 0);
			frameTimeStamp.update();
		}
	}


	private int readFromSource(byte[] buffer, int offset, int target)
			throws IOException {
		int actual_read = fis.read(buffer, offset, target);
		bytesRead += actual_read;
		if(localCopyNeeded) {
			localCopy.write(buffer, offset, actual_read);
		}
		return actual_read;
	}


	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
	}
	
	private int fillBuffer(byte [] buf,int offset, int size, InputStream fis)
	{   
		if(offset+size>buf.length) {
			throw new RuntimeException("Cannot read offset " + offset + " size " + size + " in a buffer of length " + buf.length);
		}
		
		int dlen;
		int buf_len = 0;
		int target_size = size;
		while(target_size > 0)
		{    
			try {
				dlen = readFromSource(buf, offset + buf_len, target_size);
			} catch (IOException e) {    
				Log.e(TAG, "Read streaming exception!", e);    
				return -1; 
			}   

			if(dlen >= 0){ 
				buf_len += dlen;
				target_size -= dlen;
			} else {
				return -1; 
			}   
		}   

		return size;
	} 

	//Video Ring Buffer manager
	public class VideoBuffer
	{
		private class VideoPackage
		{
			byte[] data;
			int size;
			boolean flag;
			int vflag;
			long ts;
		}
		VideoPackage[] buffers;

		int readIndex = 0;
		int writeIndex = 0;

		public VideoBuffer(int bufferNum, int pkgSize)
		{
			buffers = new VideoPackage[bufferNum];

			for(int i=0; i < bufferNum; i++){		
				VideoPackage vPkg = new VideoPackage();
				vPkg.data = new byte[pkgSize];
				buffers[i] = vPkg;
			}
			reset();
		}

		public boolean updateBuffer(int size, long ts, int vflag) {
			VideoPackage vPkg = buffers[writeIndex];
			if ( vPkg.flag == true)
				return false;

			vPkg.size = size;
			vPkg.ts = ts;
			vPkg.vflag = vflag;
			
			synchronized(this){
				vPkg.flag = true;
				updateWriteIndex();
				return true;
			}		
		}

		public byte[] nextAvailableBuffer() {
			return buffers[writeIndex].data;
		}

		public void reset()
		{			
			for(int i = 0; i < buffers.length; i++ ){
				VideoPackage vPkg = buffers[i];
				vPkg.size = 0;
				vPkg.flag = false;
				vPkg.ts = 0;
			}
			readIndex = 0;
			writeIndex = 0;			
		}

		private void updateWriteIndex()
		{
			if ( writeIndex < (buffers.length - 1) )
				writeIndex ++;
			else
				writeIndex = 0;
		}
		
		public int getVideoFlag()
		{
			VideoPackage vPkg = buffers[readIndex];
			if ( vPkg.flag == true)
				return vPkg.vflag;

			return -1;
		}
		
		public long getTimeStamp()
		{
			VideoPackage vPkg = buffers[readIndex];
			if ( vPkg.flag == true)
				return vPkg.ts;

			return 0;
		}
		public byte[] getReadBuffer()
		{
			VideoPackage vPkg = buffers[readIndex];
			if ( vPkg.flag == true)
				return vPkg.data;

			return null;
		}
		public int getReadLength()
		{
			VideoPackage vPkg = buffers[readIndex];
			if ( vPkg.flag == true)
				return vPkg.size;

			return -1;
		}
		public void releaseRead()
		{
			synchronized(this){
				VideoPackage vPkg = buffers[readIndex];
				if ( vPkg.flag == false)
					return;

				vPkg.flag = false;

				if ( readIndex < (buffers.length - 1) )
					readIndex ++;
				else
					readIndex = 0;
			}
		}
		
		public boolean isEmptySpace()
		{
			VideoPackage vPkg = buffers[writeIndex];
			if ( vPkg.flag == true)
				return false;
			
			return true;
		}
		
		public boolean writeFrame(byte[] newData, int size, long ts, int vflag)
		{					

			VideoPackage vPkg = buffers[writeIndex];
			if ( vPkg.flag == true)
				return false;

			System.arraycopy(newData, 0, vPkg.data, 0, size);							
			vPkg.size = size;
			vPkg.ts = ts;
			vPkg.vflag = vflag;
			
			synchronized(this){
				vPkg.flag = true;
				updateWriteIndex();
				return true;
			}
		}

	}

	//Frame smooth timestamp generator
	class TimeStampEstimator
	{
		final int durationHistoryLength = 2048;
		private long durationHistory[];
		int durationHistoryIndex = 0;
		long durationHistorySum = 0;				
		long lastFrameTiming = 0;			
		long sequenceDuration = 0; 
	
		public void update() 
		{
			long currentFrameTiming = SystemClock.elapsedRealtime();
			long newDuration = currentFrameTiming - lastFrameTiming;
			lastFrameTiming = currentFrameTiming;
			
			durationHistorySum -= durationHistory[durationHistoryIndex];
			durationHistorySum += newDuration;
			durationHistory[durationHistoryIndex] = newDuration;
			durationHistoryIndex ++;
			if ( durationHistoryIndex >= durationHistoryLength)
				durationHistoryIndex = 0;

			//ºÁÃëÎªµ¥Î»
			sequenceDuration += (int)(( 1.0 * durationHistorySum / durationHistoryLength));
		}
		
		public void setFirstFrameTiming()
		{
			lastFrameTiming = SystemClock.elapsedRealtime() - durationHistorySum/durationHistoryLength;
			sequenceDuration = 0;
		}

		public long getSequenceTimeStamp()
		{
			return sequenceDuration;
		}

		public void reset(long frameDuration)
		{
			if ( durationHistory == null)
				durationHistory = new long[durationHistoryLength];
			durationHistorySum = 0;
			for(int i = 0; i < durationHistoryLength; i++)
			{
				durationHistory[i] = frameDuration;			//us
				durationHistorySum += frameDuration;
			}

			lastFrameTiming = 0;
			sequenceDuration = 0;
			durationHistoryIndex = 0;
		}
		
		public TimeStampEstimator(long frameDuration)
		{
			reset( frameDuration );
		}
	}
	
	public int getBytesPerSecond() {
		long elapsedSeconds = (System.currentTimeMillis() - startTime)/1000L;
		return (int) (bytesRead/(elapsedSeconds+1));
	}
}
