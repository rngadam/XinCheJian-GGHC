package com.appdh.webcamera;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements StreamingHandler 
{
	final static String TAG = MainActivity.class.getSimpleName();
	private final String resourceDir = "/sdcard/webcamera/";
	private final String AUTODETECTION_MP4 = "/sdcard/webcamera/detect.mp4";

	//配置文件
	//GUI配置常数以及对象
	final int bg_screen_bx = 86;
	final int bg_screen_by = 128;
	final int bg_screen_width = 700;
	final int bg_screen_height = 500;
	final int bg_width = 1123;
	final int bg_height = 715;
	final int button_width = 200;
	final int button_height = 80;
	final int live_width = 640;
	final int live_height = 480;
	int screenWidth, screenHeight;
	Button btnSetup, btnStart, btnExit;
	ProgressDialog processMediaDialog;
	//主要控制对象
	OverlayView mOverlayView;				//叠加显示View
	CameraView	mCameraView;				//Live图像View对象
	
	// Streaming
	MediaSource mMediaSource;				//视频处理对象
	boolean mSetuped;						//是否检测过录像
	StreamingKernel mStreamingKernel;		//获取视频流的对象
	Thread	  mStreamingKernelThread;		//获取视频流的对象，线程对象
	WebServer mWebServer;					//内置WebServer对象
	Thread	  mWebServerThread;				//内置WebServer对象,线程对象	
	Streamer mStreamer;
	Loopback loopback = new Loopback("appdh.com", StreamingKernel.TYPICAL_FRAME_SIZE);
	
	//其他控制对象
	AudioManager mAudioManager = null;		//音频效果
	
	//Timer任务
	Timer detectCameraTimer;
	Timer rePrepareStreamTimer;
	Timer relayMessageTimer;
	private Handler mHandler = new Handler();
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			updateStatus();
			mHandler.postDelayed(mUpdateTimeTask, 1000);
		}
	};

	public void initRefreshTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}
	
	private void enableButton(Button btn)
	{
		btn.setBackgroundResource(R.drawable.button_active);
		btn.setEnabled(true);
	}
	private void disableButton(Button btn)
	{
		btn.setBackgroundResource(R.drawable.button_inactive);
		btn.setEnabled(false);
	}
	
	private void initLayout()
	{
    	//初始化显示
    	requestWindowFeature(Window.FEATURE_NO_TITLE);			//关闭标题栏
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);           
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  	//设置全屏  

        //得到显示界面尺寸
    	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();     	
    	screenWidth = display.getWidth();
    	screenHeight = display.getHeight();
        RelativeLayout.LayoutParams layoutParam = null;		//布局参数
    	LayoutInflater myInflate = null;					//构建XML分析器
    	
    	//初始化XML分析器    	
    	myInflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	//设置Top层View
    	RelativeLayout topLayout = new RelativeLayout(this);
    	setContentView(topLayout);
    	LinearLayout preViewLayout = (LinearLayout)myInflate.inflate(R.layout.topview, null);
    	layoutParam = new RelativeLayout.LayoutParams(screenWidth, screenHeight);  
    	topLayout.addView(preViewLayout, layoutParam); 
    	    
    	//设置按钮
    	int button_width_d = (int)(1.0*screenWidth/bg_width*button_width);
    	int button_height_d = (int)(1.0*screenHeight/bg_height*button_height);

    	btnSetup = new Button(this);
    	btnSetup.setWidth(button_width_d);
    	btnSetup.setHeight(button_height_d);
    	btnSetup.setBackgroundResource(R.drawable.button_active);
    	btnSetup.setText(R.string.setup);
    	layoutParam = new RelativeLayout.LayoutParams(button_width_d,button_height_d);    	
    	layoutParam.topMargin = screenHeight/4;
    	layoutParam.leftMargin = screenWidth - button_width_d*3/2;
    	topLayout.addView(btnSetup, layoutParam);
    	btnSetup.setOnClickListener(mSetupAction);
    	btnSetup.setEnabled(true);
    	
      	btnStart = new Button(this);
    	btnStart.setWidth(button_width_d);
    	btnStart.setHeight(button_height_d);
    	btnStart.setBackgroundResource(R.drawable.button_inactive);
    	btnStart.setText(R.string.start);
    	layoutParam = new RelativeLayout.LayoutParams(button_width_d,button_height_d);    	
    	layoutParam.topMargin = screenHeight/2;
    	layoutParam.leftMargin = screenWidth - button_width_d*3/2;
    	topLayout.addView(btnStart, layoutParam);        	
    	btnStart.setEnabled(false);
    	btnStart.setOnClickListener(mStartAction);
    	
    	btnExit = new Button(this);
    	btnExit.setWidth(button_width_d);
    	btnExit.setHeight(button_height_d);
    	btnExit.setBackgroundResource(R.drawable.button_active);
    	btnExit.setText(R.string.exit);
    	layoutParam = new RelativeLayout.LayoutParams(button_width_d,button_height_d);    	
    	layoutParam.topMargin = screenHeight*3/4;
    	layoutParam.leftMargin = screenWidth - button_width_d*3/2;
    	topLayout.addView(btnExit, layoutParam);    
    	btnExit.setEnabled(true);   
    	btnExit.setOnClickListener(mExitAction);
    	   
    	//计算Camera实时图像显示位置
    	int display_width_d = (int)(1.0*bg_screen_width*screenWidth/bg_width);
    	int display_height_d = (int)(1.0*bg_screen_height*screenHeight/bg_height);    	
    	int prev_rw, prev_rh;
    	if ( 1.0*display_width_d/display_height_d > 1.0*live_width/live_height){    		
    		prev_rh = display_height_d;
    		prev_rw = (int)(1.0*display_height_d * live_width/live_height);
    	} else {
    		prev_rw = display_width_d;
    		prev_rh = (int)(1.0*display_width_d*live_height/live_width);
    	}
    	layoutParam = new RelativeLayout.LayoutParams(prev_rw, prev_rh);
    	layoutParam.topMargin = (int)(1.0*bg_screen_by*screenHeight/bg_height );
    	layoutParam.leftMargin = (int)(1.0*bg_screen_bx*screenWidth/bg_width );
    	
    	//增加预览图像实时显示
    	mCameraView = new CameraView(this, null);
    	topLayout.addView(mCameraView, layoutParam); 
 
    	//增加OverlayView叠加显示    	
    	mOverlayView = new OverlayView(this,null);
    	topLayout.addView(mOverlayView, layoutParam); 
    	if(!mSetuped){
    		mOverlayView.addMessage(R.string.setup_camera);
    		enableButton(btnSetup);
    		disableButton(btnStart);
    		enableButton(btnExit);
    	} else {
    		mOverlayView.addMessage(R.string.setup_ok);
    		enableButton(btnSetup);
    		enableButton(btnStart);
    		enableButton(btnExit);
    	}
	}
	
	private Handler showRelayMessageHandler = new Handler()
	{
		@Override
        public void handleMessage(Message msg) {    
			if(msg.what != 0) {
				mOverlayView.addMessage(msg.what);	
			} else {
				mOverlayView.setStatus((String) msg.obj);
			}
			
			mOverlayView.postInvalidate();
		}
	};
	
	private Handler beginProcessCameraHandler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) {        	
			String text = ((Context)MainActivity.this).getString(R.string.setup_process);
			processMediaDialog = ProgressDialog.show(MainActivity.this, "", 
					text, true);
			processMediaDialog.show();
    		
    		Thread processThread = new Thread()
    		{
    	    	@Override
    			public void run(){
    	    		if(!Streamer.detectSetuped(AUTODETECTION_MP4)) {
    	    			Log.e(TAG, "Could not complete auto-detection");
    	    		}    	    		
    	    		endProcessCameraHandler.sendEmptyMessage(0);
    	    	}
    	    };
    	    processThread.start();
         }
	};
	
	private Handler endProcessCameraHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {        	
			if(Streamer.isValid()){
				mOverlayView.addMessage(R.string.setup_ok);
				enableButton(btnStart);
			} else {
				mOverlayView.addMessage(R.string.setup_error);
			}
			mOverlayView.postInvalidate();
			processMediaDialog.dismiss();
		}
	};

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
	
	private void buildResource()
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
        	copyResourceFile(R.raw.index, resourceDir + "index.html"  );
        	copyResourceFile(R.raw.player, resourceDir + "player.swf"  );
        	copyResourceFile(R.raw.player, resourceDir + "player2.swf"  );
        	copyResourceFile(R.raw.yt, resourceDir + "yt.swf"  );        	
        }
        catch (IOException e) {
        	Log.e(TAG, "Error writing out resources", e);
        }
    	Log.d(TAG, "Successfully extracted and wrote WebServer resources");
	}
	
	private void startDetectCamera()
	{
		mOverlayView.addMessage(R.string.setup_wait);
		mOverlayView.postInvalidate();
		
		if(!mMediaSource.initMedia()) {
			Log.e(TAG, "Could not init MediaSource!");
			return;
		}
		mMediaSource.prepareOutput(AUTODETECTION_MP4);		
		mMediaSource.startCapture();
		TimerTask stopDetectCameraTask = new TimerTask()
	    {
			@Override
			public void run()
	    	{
	    		mMediaSource.stopCapture();
	    		mMediaSource.releaseMedia();	
	    		beginProcessCameraHandler.sendEmptyMessage(0);
	    	}
	    };
	    
		//Timer任务
		if(detectCameraTimer == null ){						
			detectCameraTimer = new Timer();
		}		
		detectCameraTimer.schedule(stopDetectCameraTask, 3000);		
	}
	
	public void relayMessage(final int msg)
	{
		showRelayMessageHandler.sendEmptyMessage(msg);
	}
	
	public void setStatus(final String status) {
		Message msg = showRelayMessageHandler.obtainMessage(0, status);
		showRelayMessageHandler.sendMessage(msg);
	}
    
    void rePrepareStreaming()
    {
    	TimerTask rePrepareStreamingTask = new TimerTask()
	    {
	    	@Override
			public void run()
	    	{
	    		prepareStreaming();
	    	}
	    };
	    
    	//Timer任务
		if(rePrepareStreamTimer == null ){						
			rePrepareStreamTimer = new Timer();
		}		
		rePrepareStreamTimer.schedule(rePrepareStreamingTask, 1000);	
    }
    
    void stopStreaming()
    {
    	mStreamingKernel.stopStreaming();
    	mMediaSource.stopCapture();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopStreaming();
	}

    private void prepareStreaming()
    {
		if(mStreamingKernel == null)
			mStreamingKernel = new StreamingKernel(loopback, 60, false);		
		if(mStreamer == null)
			mStreamer = new Streamer(mStreamingKernel);
		
		mStreamingKernel.repareStreaming();
		
		Log.d(TAG, "Initing media source and media source output");
		mMediaSource.initMedia();			
		mMediaSource.prepareOutput(loopback.getTargetFileDescriptor());			
		
		Log.d(TAG, "Spawning Streaming Kernel thread");
		mStreamingKernelThread = new Thread( mStreamingKernel );			
		mStreamingKernelThread.start();
    }
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        //检查是否探测过摄像头
        mSetuped = Streamer.detectSetuped(AUTODETECTION_MP4);

        //初始化GUI设置
        initLayout();  									        
        //初始化Stream对象
        mMediaSource = new MediaSource(mCameraView);	        
        //设置音频装置
        if (mAudioManager == null) {					
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        }                
		buildResource();
    }
    
    private void startWork()
    {    	
		disableButton(btnStart);
		disableButton(btnSetup);
		
		//启动 Web服务对象
		mWebServer = new WebServer(MainActivity.this);		
		mWebServerThread = new Thread( mWebServer );
		mWebServerThread.start();
		String url = "http:/" + WebServer.getInterfaces() + ":8080";
		
		mOverlayView.addMessage( (MainActivity.this).getString(R.string.start_server) + url);
		mOverlayView.postInvalidate();
		
		prepareStreaming();	
		
    }
    
    private OnClickListener mStartAction = new OnClickListener() 
    {
		@Override
		public void onClick(View v) {			
			startWork();
		}
    };

    private OnClickListener mSetupAction = new OnClickListener() 
    {
		@Override
		public void onClick(View v) {
			disableButton(btnSetup);
			startDetectCamera();
		}
    };
    
    private OnClickListener mExitAction = new OnClickListener() 
    {
		@Override
		public void onClick(View v) {
			finish();
		}
    };

    void updateStatus() {
		setStatus("Bytes/s: " + mStreamingKernel.getBytesPerSecond() + " FPS: " + mStreamer.getFramesPerSecond() + " Overflow: " + mStreamingKernel.getOverflow());
    }
    
	/* (non-Javadoc)
	 * @see com.appdh.webcamera.StreamingHandler#doStreaming(java.io.OutputStream)
	 */
	@Override
	public void doStreaming(OutputStream outputStream) {
		Log.d(TAG, "Starting capture");
		initRefreshTimer();
		mMediaSource.startCapture();
		mStreamer.doStreaming(outputStream);
		stopStreaming();    	
		rePrepareStreaming();    
	}
}