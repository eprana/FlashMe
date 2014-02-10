package com.imac.FlashMe;

import com.imac.FlashMe.VuforiaApp.utils.SampleApplicationGLView;
import com.imac.VuforiaApp.SampleApplicationControl;
import com.imac.VuforiaApp.SampleApplicationException;
import com.imac.VuforiaApp.SampleApplicationSession;
import com.qualcomm.vuforia.State;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class GameActivity  extends Activity implements SampleApplicationControl {
	
	SampleApplicationSession vuforiaAppSession;
	private SampleApplicationGLView mGlView;
	
	static {
		System.loadLibrary("Vuforia");
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		vuforiaAppSession = new SampleApplicationSession(this);
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public boolean doInitTrackers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doLoadTrackersData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doStartTrackers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doStopTrackers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doUnloadTrackersData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doDeinitTrackers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onInitARDone(SampleApplicationException e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQCARUpdate(State state) {
		// TODO Auto-generated method stub
		
	}

}
