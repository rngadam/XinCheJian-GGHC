package com.appdh.webcamera;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;


class MediaPackage
{
	private final static String TAG = MediaPackage.class.getSimpleName();
	
	private static final byte[] FlvHeader = {
		// FLV header
			// F L V
			(byte)0x46,(byte)0x4c,(byte)0x56,
			// file version 1
			(byte)0x01,
			// video tags present
			(byte)0x01,
			// data offset
			(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x09,
			// previous tag size
			(byte)0x00,(byte)0x00,(byte)0x00, (byte)0x00,
		// Meta tag
		(byte)0x12, (byte)0x00,(byte)0x00,(byte)0xb6,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x0a,
		// o n M e t a D a t a
		(byte)0x6f,(byte)0x6e,(byte)0x4d,(byte)0x65,(byte)0x74,(byte)0x61,(byte)0x44,(byte)0x61,(byte)0x74,(byte)0x61,
		//
		(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x07,(byte)0x00,(byte)0x08,(byte)0x64,(byte)0x75,(byte)0x72,(byte)0x61
		,(byte)0x74,(byte)0x69,
		(byte)0x6f,(byte)0x6e,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x77
		,(byte)0x69,(byte)0x64,(byte)0x74,(byte)0x68,(byte)0x00,(byte)0x40,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x68
		,(byte)0x65,(byte)0x69,(byte)0x67,(byte)0x68,(byte)0x74,(byte)0x00,(byte)0x40,(byte)0x7e,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x09
		// f r a me r a t e
		,(byte)0x66,(byte)0x72,(byte)0x61,(byte)0x6d,(byte)0x65,(byte)0x72,(byte)0x61,(byte)0x74,(byte)0x65,
		// framerate double value = 11 bytes = value 12 fps
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x0c,
		// v i d e o c o d e c i d
		(byte)0x76,(byte)0x69,(byte)0x64,(byte)0x65,(byte)0x6f,(byte)0x63,(byte)0x6f,(byte)0x64,(byte)0x65,(byte)0x63,(byte)0x69,(byte)0x64
		// value
		,(byte)0x00,(byte)0x40,(byte)0x1c,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0d,
		// v i d e o d a t a r a t e 
		(byte)0x76,(byte)0x69,(byte)0x64,(byte)0x65,(byte)0x6f,(byte)0x64,(byte)0x61,(byte)0x74,(byte)0x61,(byte)0x72,(byte)0x61,(byte)0x74,(byte)0x65,
		// value, 11 bytes, kbps, set to 7 kbps
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x07,
		//
		(byte)0x65,(byte)0x6e,(byte)0x63,(byte)0x6f,(byte)0x64,(byte)0x65,(byte)0x72,(byte)0x02,(byte)0x00,(byte)0x0b,(byte)0x4c,(byte)0x61,(byte)0x76
		,(byte)0x66,(byte)0x35,(byte)0x32,(byte)0x2e,(byte)0x38,(byte)0x37,(byte)0x2e,(byte)0x31,(byte)0x00,(byte)0x08,
			// F I L E S I Z E
			(byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65, (byte)0x73,(byte)0x69,(byte)0x7a,(byte)0x65,
				// double value, 11 bytes
				(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
				(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
				(byte)0x00,(byte)0x00,(byte)0x00,
		// Next?
		(byte)0x09,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xc1
	};
	
	static public void writeFlvHeader(OutputStream out) throws IOException {
		out.write(FlvHeader);
	}
	
	static public void writeVideoHeader(OutputStream out, byte[] sps, byte[] pps) throws IOException
	{
		//FLV_TAG_TYPE_VIDEO
		//{
		out.write((byte)0x09);			
		//	frame data size
		out.write(0); 
		out.write(0 ); 
		out.write((byte)(16 + sps.length + pps.length));
		//	frame timestamp
		out.write(0);
		out.write(0);
		out.write(0);
		out.write(0);
		//	StreamID
		out.write(0);
		out.write(0);
		out.write(0);
		//	frame data begin
		//	{
		//		Frametype and CodecID
		out.write((byte)0x17);
		out.write(0x00);
		//		Composition time
		out.write(0x00);
		out.write(0x00);
		out.write(0x00);
		
		// VideoData->Data
		//		Version
		out.write(0x01);
		//		profile&level
		out.write(sps[1]);
		out.write(sps[2]);
		out.write(sps[3]);
		//		reserved
		out.write((byte)0xff);
		out.write((byte)0xe1);
		//		sps_size&data
		out.write((byte)(sps.length >> 8));
		out.write((byte)(sps.length & 0xFF));
		for(int i = 0; i < sps.length; i++)
			out.write(sps[i]);
		//		pps_size&data
		out.write(0x01);
		out.write((byte)(pps.length >> 8));
		out.write((byte)(pps.length & 0xFF));
		for(int i = 0; i < pps.length; i++)
			out.write(pps[i]);
		//	}
		//	LastTagsize
		out.write(0);
		out.write(0);
		out.write(0);
		out.write((byte)(27+sps.length+(pps.length)));
		//}
	}

	static int writeFlvPackage(OutputStream out, byte[] nal, int nalLength, long ts, int flag) throws IOException
	{
		//frame tag
		out.write((byte)0x09);
		//frame data length
		out.write((byte)(((nalLength + 5)>>16)&0xff));
		out.write((byte)(((nalLength + 5)>>8)&0xff));
		out.write((byte)((nalLength + 5)&0xff));
		//frame time stamp
		out.write((byte)((ts>>16)&0xff));
		out.write((byte)((ts>>8)&0xff));
		out.write((byte)(ts&0xff));
		//frame time stamp extend
		out.write((byte)((ts>>24)&0xff));
		//frame reserved
		out.write(0);
		out.write(0);
		out.write(0);


		if ( flag == 1) {
			Log.v(TAG, "Adding AVC keyframe");
			out.write((byte)0x17);
		}
	   	else {
			Log.v(TAG, "Adding AVC interframe");
			out.write((byte)0x27);
	   	}

		out.write(1);
		
		out.write(0);
		out.write(0);
		out.write(0);
		
		for(int i = 0; i < nalLength; i++) {
			out.write(nal[i]);
		}

		int tl = 16 + nalLength;
		out.write((byte)((tl>>24)&0xff));  
		out.write((byte)((tl>>16)&0xff));  
		out.write(((byte)((tl>>8)&0xff)));  
		out.write((byte)(tl&0xff));  

		return 20 + nalLength;
	}
	

}
