package com.appdh.webcamera;

import java.io.OutputStream;

public interface StreamingHandler {

	public abstract void doStreaming(OutputStream outputStream);

}