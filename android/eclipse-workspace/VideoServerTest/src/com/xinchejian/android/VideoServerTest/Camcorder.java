package com.xinchejian.android.VideoServerTest;

import java.io.IOException;
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

	public Camcorder(int port) throws UnknownHostException, IOException {
		this.port = port;
	}

	public void startRecordingToDisk(String filename, SurfaceHolder surfaceHolder) {
		initMediaRecorder(surfaceHolder);
		recorder.setOutputFile(filename);
		startRecorder();		
	}
	
    private void initMediaRecorder(SurfaceHolder surfaceHolder) {
    	camera = Camera.open();
		Camera.Parameters p = camera.getParameters();
		p.setPreviewSize(320, 240);
		p.set("orientation", "landscape");
		p.set("rotation", 90);
		p.setPreviewFormat(PixelFormat.JPEG);
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
		recorder.setVideoFrameRate(15);
		recorder.setMaxDuration(-1); // disable limit
		recorder.setMaxFileSize(999999999);
	}

	public void startRecordingToNetwork(SurfaceHolder surfaceHolder) throws IOException
    {
		initMediaRecorder(surfaceHolder);

		serverSocket = new ServerSocket(port);
    	Socket clientSocket = serverSocket.accept();
    	ParcelFileDescriptor fd = ParcelFileDescriptor.fromSocket(clientSocket);
    	recorder.setOutputFile(fd.getFileDescriptor());

    	startRecorder();
    }

	private void startRecorder() {
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
		}

    	recorder.start();
	}
    
    public void stopRecording()
    {
    	if(recorder != null) {
	    	recorder.stop();
	    	recorder.release();
    	}
    	if(camera != null) {
    		camera.lock();
    	}
    }
}