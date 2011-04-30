package com.xinchejian.android.Robot;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;

public class Camcorder {

	MediaRecorder recorder;
	SurfaceHolder holder;
	String outputFile;
	private Socket clientSocket;

	public Camcorder(String ip, int port) throws UnknownHostException,
			IOException {
		clientSocket = new Socket(ip, port);
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
		// recorder.setVideoSize(480, 320);
		// recorder.setVideoFrameRate(15);
		// recorder.setMaxDuration(10000);
	}

	public void startRecording() {
		ParcelFileDescriptor fd = ParcelFileDescriptor.fromSocket(clientSocket);
		recorder.setOutputFile(fd.getFileDescriptor());
		recorder.setOutputFile(outputFile);
		if (recorder != null) {
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				Log.e("IllegalStateException", e.toString());
			} catch (IOException e) {
				Log.e("IOException", e.toString());
			}
		}

		recorder.start();
	}

	public void stopRecording() {
		recorder.stop();
		recorder.release();
	}
}