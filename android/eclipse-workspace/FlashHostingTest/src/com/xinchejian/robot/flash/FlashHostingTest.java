package com.xinchejian.robot.flash;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class FlashHostingTest extends Activity {
	private JSInterface myJSInterface;
	private WebView webView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		webView = (WebView)findViewById(R.id.webView1);
		
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setJavaScriptEnabled(true);
		
		myJSInterface = new JSInterface(webView);
		final Context myApp = this; 
		webView.setWebViewClient(new WebViewClient() {
		    @Override
	       public void onPageFinished(WebView view, String url){
		    	System.out.println("LOADED!");
		    	webView.loadUrl("javascript:testEcho(\'Hello\')");
	        }	
		});
		
		webView.setWebChromeClient(new WebChromeClient() {  
		    @Override  
		    public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)  
		    {  
		        new AlertDialog.Builder(myApp)  
		            .setTitle("javaScript dialog")  
		            .setMessage(message)  
		            .setPositiveButton(android.R.string.ok,  
		                    new AlertDialog.OnClickListener()  
		                    {  
		                        public void onClick(DialogInterface dialog, int which)  
		                        {  
		                            result.confirm();  
		                        }  
		                    })  
		            .setCancelable(false)  
		            .create()  
		            .show();  
		  
		        return true;  
		    } 
		    
		});  
		// register class containing methods to be exposed to JavaScript
		webView.addJavascriptInterface(myJSInterface, "JSInterface"); 
		//webView.loadDataWithBaseURL("fake://fake/fake", htmlPre+htmlCode+htmlPost, "text/html", "UTF-8", null);  
		//webView.loadUrl("file:///sdcard/main.html");  
		webView.loadUrl("file:///android_asset/main.html");
		//webview.loadDataWithBaseURL( baseUrl, htmlText, "text/html", "UTF-8", null);
		webView.loadUrl("javascript:testEcho(\'Hello\')");
    }
    
	public class JSInterface {

		private WebView mAppView;
		public JSInterface  (WebView appView) {
		        this.mAppView = appView;
		    }

		    public void doEchoTest(String echo){
		        Toast toast = Toast.makeText(mAppView.getContext(), echo, Toast.LENGTH_SHORT);
		        toast.show();
		    }
	}
	
	
}