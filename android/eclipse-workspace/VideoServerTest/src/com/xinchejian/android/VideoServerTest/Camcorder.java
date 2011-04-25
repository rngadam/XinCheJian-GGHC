package com.xinchejian.android.VideoServerTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;

public class Camcorder  {

	MediaRecorder recorder;
	private Camera camera;
	private ServerSocket serverSocket;
	private final int port;
	private SurfaceHolder surfaceHolder;
	private Socket clientSocket;
	private final static String SAMPLE_FILENAME = "/data/videos/sample.mp4";

	public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
	}

	public Camcorder(int port) throws UnknownHostException, IOException {
		this.port = port;
	}

	private void recordSampleToDisk() {
		initMediaRecorder();
		recorder.setOutputFile(SAMPLE_FILENAME);
		startRecorder();	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopRecording();
	}
	
    private void initMediaRecorder() {
    	camera = Camera.open();
		Camera.Parameters p = camera.getParameters();
		/*p.setPreviewSize(320, 240);
		p.setPreviewFormat(PixelFormat.YCbCr_422_SP);
		p.setPictureFormat(PixelFormat.YCbCr_422_SP);*/
		p.set("orientation", "landscape");
		p.set("rotation", 90);
		camera.setParameters(p);
		
    	camera.unlock();
    	
		recorder = new MediaRecorder();
		recorder.setCamera(camera);
		recorder.setPreviewDisplay(surfaceHolder.getSurface());
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		recorder.setVideoSize(640, 480);
		recorder.setVideoFrameRate(27);
		recorder.setMaxDuration(5000); // disable limit
		recorder.setMaxFileSize(999999999);
	}

	public void startRecordingToNetwork()
    {
		new Thread(new Runnable() {

			public void run()
		    {
				recordSampleToDisk();
				try {
					serverSocket = new ServerSocket(port);
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					Log.e(this.getClass().getSimpleName(), "failed to receive client", e);
					return;
				}
				Log.d(this.getClass().getSimpleName(), "Connection received from: " + clientSocket.getRemoteSocketAddress());
				OutputStream outputStream;
				try {
					outputStream = clientSocket.getOutputStream();
				} catch (IOException e) {
					Log.e(this.getClass().getSimpleName(), "Could not get client output stream", e);
					return;
				}
				File file = new File(SAMPLE_FILENAME);
				byte buffer[] = new byte[(int)file.length()];
				try {
					new FileInputStream(file).read(buffer);
				} catch (FileNotFoundException e) {
					Log.e(this.getClass().getSimpleName(), "model file not found", e);
					return;
				} catch (IOException e) {
					Log.e(this.getClass().getSimpleName(), "model file could not be read", e);
					return;
				}
				try {
					outputStream.write(buffer);
				} catch (IOException e) {
					Log.e(this.getClass().getSimpleName(), "error writing model file to stream", e);
					return;
				}
		    }
	    }).start();
    }
	
	public boolean isConnected() {
		return clientSocket != null;
	}
	
	private void initNetworkRecorder() {
		initMediaRecorder();
		
    	ParcelFileDescriptor fd = ParcelFileDescriptor.fromSocket(clientSocket);
    	recorder.setOutputFile(fd.getFileDescriptor());
	
    	startRecorder();
	}
	
	private void startRecorder() 
	{
	
		if (recorder != null) {
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				Log.e(this.getClass().getSimpleName(), "failed to prepare", e);
				return;
			} catch (IOException e) {
				Log.e(this.getClass().getSimpleName(), "Error preparing camera", e);
				return;
			}
			recorder.start();
		}
	}
    
	public void startRecording() {
		initNetworkRecorder();
	}
	
    public void stopRecording()
    {
    	if(recorder != null) {
    		try {
    			recorder.stop();
    		} catch(RuntimeException e) {
    			 e.printStackTrace();
    		}
    		//recorder.release();
    	}
    	if(camera != null) {
    		camera.lock();
    	}
    }
}