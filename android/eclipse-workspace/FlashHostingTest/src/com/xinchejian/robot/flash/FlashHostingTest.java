package com.xinchejian.robot.flash;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

public class FlashHostingTest extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
			copyResourceFile(R.raw.hyperheart, "/sdcard/HyperHeart.swf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webView = (WebView)findViewById(R.id.webView1);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setPluginsEnabled(true);
		webView.loadDataWithBaseURL("fake://fake/fake", htmlPre+htmlCode+htmlPost, "text/html", "UTF-8", null);  
    }
    
	private void copyResourceFile(int rid, String targetFile) throws IOException
	{
		InputStream fin = ((Context)this).getResources().openRawResource(rid);
		FileOutputStream fos = new FileOutputStream(targetFile);  
    	
		int     length;
		byte[] buffer = new byte[1024*32]; 
		while( (length = fin.read(buffer)) != -1){
			fos.write(buffer,0,length);
		}
		fin.close();
		fos.close();
	}   
	WebView webView;
	String htmlPre = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"></head><body style='margin:0; pading:0; background-color: black;'>";  
	String htmlCode = 
			" <embed style='width:100%; height:100%' src=\"file:///sdcard/HyperHeart.swf\"" +
			"  autoplay='true' " +
			"  quality='high' bgcolor='#000000' " +
			"  name='VideoPlayer' align='middle'" + // width='640' height='480' 
			"  allowScriptAccess='*' allowFullScreen='true'" +
			"  type='application/x-shockwave-flash' " +
			"  pluginspage='http://www.macromedia.com/go/getflashplayer' />" +
			"";
	String htmlPost = "</body></html>";
	
}