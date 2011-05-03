package com.appdh.webcamera;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

public class Resources {
	private final static String TAG = Resources.class.getSimpleName();
	private final Context context;
	private final String resourceDir;

	public Resources(Context context, String resourceDir) {
		this.context = context;
		this.resourceDir = resourceDir;
		
	}
	
	public void copyResourceFile(int rid, String targetFile) throws IOException
	{
		InputStream fin = context.getResources().openRawResource(rid);
		FileOutputStream fos = new FileOutputStream(targetFile);  
    	
		int     length;
		byte[] buffer = new byte[1024*32]; 
		while( (length = fin.read(buffer)) != -1){
			fos.write(buffer,0,length);
		}
		fin.close();
		fos.close();
	}
	
	public void copyAssetFile(String targetFile, String targetDir) throws IOException
	{
		InputStream fin = context.getAssets().open(targetFile);
		FileOutputStream fos = new FileOutputStream(targetDir + targetFile);  
    	
		int     length;
		byte[] buffer = new byte[1024*32]; 
		while( (length = fin.read(buffer)) != -1){
			fos.write(buffer,0,length);
		}
		fin.close();
		fos.close();
	}
	
	public void buildResource()
	{
		String[] str ={"mkdir", resourceDir};

        try { 
        	Process ps = Runtime.getRuntime().exec(str);
        	try {
        		ps.waitFor();
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	} 
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        
        Log.d(TAG, "Writing out resources");
        //((Context)this).getResources().openRawResource(id)
        //将资源写入SD卡对应目录
        try { 
        	copyAssetFile("index.html", resourceDir);
        	copyAssetFile("player2.swf", resourceDir);
         	copyAssetFile("robotAir.swf", resourceDir); 
         	copyAssetFile("swfobject.js", resourceDir);       
        }
        catch (IOException e) {
        	Log.e(TAG, "Error writing out resources", e);
        }
    	Log.d(TAG, "Successfully extracted and wrote WebServer resources");
	}
}
