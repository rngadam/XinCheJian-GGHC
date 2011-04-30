package com.appdh.webcamera;

import java.io.*;

import android.util.Log;

class MediaDetect
{
	private final static String TAG = MediaSource.class.getSimpleName();
	private static final int MAX_HEADER_LENGTH = 1024*4;
	private static byte[] checkBuffer = new byte[MAX_HEADER_LENGTH];
	
	public static int checkMP4_MDAT(InputStream is) throws IOException
	{
		int fpos = 0;
		int fms = 0;
		boolean isOK = false;
		int n;
		while(true){				
			n = is.read();
			if(n != -1) {
				fpos++;
				switch(n)
				{
				case (byte)'m':
					if( fms == 0)
						fms = 1;
					else
						fms = 0;
					break;
				
				case (byte)'d':
					if( fms == 1)
						fms = 2;
					else
						fms = 0;
					break;
					
				case (byte)'a':
					if( fms == 2)
						fms = 3;
					else
						fms = 0;
					break;
				
				case (byte)'t':
					if ( fms == 3)
						isOK = true;
					else
						fms = 0;
					break;
					
				default:
					fms = 0;
					break;					
				}
				if( isOK ){
					Log.d(TAG, "MP4 file MDAT position is: " + fpos);	
					return fpos;
				}
				if(fpos >= MAX_HEADER_LENGTH) {
					throw new IOException("No MDAT found after looking at " + fpos + " bytes");	
				}
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	public static boolean checkMP4_MOOV(String fileName) throws IOException
	{
		File fin = new File(fileName);
		
		if( fin.isFile() ){
			InputStream is = new FileInputStream(fin.getAbsolutePath());
			int pos;
			int fms = 0;
			boolean isOK;
			int n;
			while(true){				
				isOK = false;
				
				n = is.read(checkBuffer);
				if ( n < 0){
					break;
				}
				
				for(pos = 0; pos < n; pos++){
					byte tmp = checkBuffer[pos];
					switch(tmp)
					{
					case (byte)'a':
						if( fms == 0)
							fms = 1;
						else
							fms = 0;
						break;
					
					case (byte)'v':
						if( fms == 1)
							fms = 2;
						else
							fms = 0;
						break;
						
					case (byte)'c':
						if( fms == 2)
							fms = 3;
						else
							fms = 0;
						break;
					
					case (byte)'C':
						if ( fms == 3)
							fms = 4;
						else
							fms = 0;
						break;
					case (byte)0x01:
						if ( fms == 4)
							fms = 5;
						else
							fms = 0;
						break;
					default:
						fms = 0;
						break;					
					}
					if(fms == 5){
						isOK = true;
						break;
					}
				}				
				if( isOK ){
			        Log.d(TAG,"MP4 file SPS PPS is OK.**************");
					//开始获取PPS以及SPS数据	
					for(int i=0; i < checkBuffer.length - pos; i++)
					{
						checkBuffer[i] = checkBuffer[i+pos];
					}
					if ( pos > checkBuffer.length - MAX_HEADER_LENGTH)
					{
						is.read(checkBuffer, checkBuffer.length - pos, MAX_HEADER_LENGTH);
					}
					
					return true;
				}
			}	
		}
		return false;	
	}

}


