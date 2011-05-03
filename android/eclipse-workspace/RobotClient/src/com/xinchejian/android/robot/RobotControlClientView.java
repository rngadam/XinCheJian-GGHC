package com.xinchejian.android.robot;

import com.xinchejian.android.bluetooth.RobotControl;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class RobotControlClientView extends LinearLayout {

	private int updown_pos = RobotControl.UPDOWN_DEFAULT_POS;
	private int sideways_pos = RobotControl.SIDEWAYS_DEFAULT_POS;

	private SeekBar xAxisSeekBar;
	private SeekBar zAxisSeekBar;	
	private TextView upDownTextView;
	private TextView sidewaysTextView;
	private OnControlUpdateListener listener;
	
	public RobotControlClientView(Context context) {
		super(context);
		inflate(context, R.layout.main, this);
		
		xAxisSeekBar = (SeekBar) findViewById(R.id.UpDownSeekBar);
		zAxisSeekBar = (SeekBar) findViewById(R.id.SidewaysSeekBar);
		upDownTextView = (TextView) findViewById(R.id.UpDownTextView);
		sidewaysTextView = (TextView) findViewById(R.id.SidewaysTextView);
		
		xAxisSeekBar.setMax(RobotControl.UPDOWN_MAX_POS);
		zAxisSeekBar.setMax(RobotControl.SIDEWAYS_MAX_POS);
		xAxisSeekBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(final SeekBar seekBar,
							final int progress, final boolean fromUser) {
						if (fromUser) {
							if (progress < RobotControl.UPDOWN_MIN_POS) {
								updown_pos = RobotControl.UPDOWN_MIN_POS;
							} else {
								updown_pos = progress;
							}
						}
						updateValues();
					}

					@Override
					public void onStartTrackingTouch(final SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(final SeekBar seekBar) {
						upDownUpdate();
					}
				});
		zAxisSeekBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(final SeekBar seekBar,
							final int progress, final boolean fromUser) {
						if (fromUser) {
							if (progress < RobotControl.SIDEWAYS_MIN_POS) {
								sideways_pos = RobotControl.SIDEWAYS_MIN_POS;
							} else {
								sideways_pos = progress;
							}

						}
						updateValues();
					}

					@Override
					public void onStartTrackingTouch(final SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(final SeekBar seekBar) {
						sidewaysUpdate();
					}
				});
	}

	private void upDownUpdate() {
		if(listener != null)
			listener.onUpDownUpdate(updown_pos);
	}
	
	private void sidewaysUpdate() {
		if(listener != null)
			listener.onSidewaysUpdate(sideways_pos);	
	}
	
	public void setOnControlUpdatelistener(OnControlUpdateListener listener) {
		this.listener = listener;
		sidewaysUpdate();
		upDownUpdate();
	}
	
	public interface OnControlUpdateListener {
		 public abstract void onSidewaysUpdate(int x);
		 public abstract void onUpDownUpdate(int x);
	} 
	
	private void updateValues() {
		upDownTextView.setText("" + updown_pos);
		sidewaysTextView.setText("" + sideways_pos);
		xAxisSeekBar.setProgress(updown_pos);
		zAxisSeekBar.setProgress(sideways_pos);
	}
}
