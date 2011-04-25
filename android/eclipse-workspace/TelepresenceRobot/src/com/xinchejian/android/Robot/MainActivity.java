package com.xinchejian.android.Robot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends Activity implements InformationSource {
	private WifiManager wifiManager;
	private Handler mHandler = new Handler();
	private GraphicsView graphicsView;

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			graphicsView.postInvalidate();
			mHandler.postDelayed(mUpdateTimeTask, 100);
		}
	};

	public void initRefreshTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	public String getIp() {
		return "IP: " + intToIp(wifiManager.getConnectionInfo().getIpAddress());
	}

	public static String intToIp(int i) {
		return (i & 0xFF) + "." + (i >> 8 & 0xFF) + "." + (i >> 16 & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		graphicsView = new GraphicsView(this, this);
		setContentView(graphicsView);
		startService(new Intent(this, RobotControlService.class));
		wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		initRefreshTimer();
	}

	private static class GraphicsView extends View {

		private Paint iris;
		private Paint iris_center;
		private Paint cornea;
		private Paint lips;
		private Paint teeths;
		private float unit;
		private final InformationSource source;
		private Paint text;

		public GraphicsView(Context context, InformationSource source) {
			super(context);
			this.source = source;
			iris = new Paint();
			iris.setColor(Color.BLUE);
			iris.setAntiAlias(true);

			iris_center = new Paint();
			iris_center.setColor(Color.BLACK);
			iris_center.setAntiAlias(true);

			cornea = new Paint();
			cornea.setColor(Color.WHITE);
			cornea.setAntiAlias(true);

			lips = new Paint();
			lips.setColor(Color.RED);
			lips.setAntiAlias(true);

			teeths = new Paint();
			teeths.setColor(Color.WHITE);
			teeths.setAntiAlias(true);

			text = new Paint();
			text.setColor(Color.WHITE);
			text.setTextSize(20);
			text.setAntiAlias(true);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			unit = getWidth() / 10;

			// left eye
			drawEye(canvas, 2 * unit, unit);
			// right eye
			drawEye(canvas, getWidth() - 2 * unit, unit);

			// mouth
			drawMouth(canvas, getWidth() / 2, (int) (getHeight() * 0.70f));
			canvas.drawText(source.getIp(), 10, 10, lips);
		}

		private void drawMouth(Canvas canvas, int x, int y) {
			canvas.drawRect(x - 2f * unit, y - unit, x + 2f * unit, y + unit,
					lips);
			canvas.drawRect(x - 1.75f * unit, y - 0.75f * unit, x + 1.75f
					* unit, y + 0.75f * unit, teeths);
		}

		private void drawEye(Canvas canvas, float x, float y) {
			canvas.drawCircle(x, y, unit, cornea);
			canvas.drawCircle(x, y, 0.75f * unit, iris);
			canvas.drawCircle(x, y, 0.3f * unit, iris_center);
		}
	}
}
