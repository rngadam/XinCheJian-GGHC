package com.xinchejian.android.robot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AnimationControlView extends WebView {
	public static final String TAG = AnimationControlView.class.getSimpleName();
	private JSInterface myJSInterface;

	/** Called when the activity is first created. */
	public AnimationControlView(Context context) {
		super(context);
		// needed configuration
		getSettings().setAllowFileAccess(true);
		getSettings().setPluginsEnabled(true);
		getSettings().setJavaScriptEnabled(true);
		// only start calling Javascript when page fully loaded
		setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d(TAG, "Paged loaded");
			}
		});

		final Context myContext = context;
		// enable "Javascript" alerts
		myJSInterface = new JSInterface(this);
		
		setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final android.webkit.JsResult result) {
				new AlertDialog.Builder(myContext)
						.setTitle("javaScript dialog")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								}).setCancelable(false).create().show();

				return true;
			}

		});
		// register class containing methods to be exposed to JavaScript
		addJavascriptInterface(myJSInterface, "JSInterface");
		loadUrl("file:///android_asset/control.html");
	}

	public class JSInterface {

		private WebView mAppView;

		public JSInterface(WebView appView) {
			this.mAppView = appView;
		}
		public void initCompleted() {
			Log.d(TAG, "Init completed, JSInterface callback");
			loadUrl("javascript:mouthTalk(5)");
		}
		public void doEchoTest(String echo) {
			Toast toast = Toast.makeText(mAppView.getContext(), echo,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

}