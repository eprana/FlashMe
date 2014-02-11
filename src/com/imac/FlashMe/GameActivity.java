package com.imac.FlashMe;

import java.util.Vector;

import com.imac.VuforiaApp.SampleApplicationControl;
import com.imac.VuforiaApp.SampleApplicationException;
import com.imac.VuforiaApp.SampleApplicationSession;
import com.imac.VuforiaApp.utils.LoadingDialogHandler;
import com.imac.VuforiaApp.utils.SampleApplicationGLView;
import com.imac.VuforiaApp.utils.Texture;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class GameActivity  extends Activity implements SampleApplicationControl {
	
	private static final String LOGTAG = "Game";
	
	SampleApplicationSession vuforiaAppSession;
	private SampleApplicationGLView mGlView;
	private GameRenderer mRenderer;
	
	private Vector<Texture> mTextures;
    private RelativeLayout mUILayout;
    
	private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mIsFrontCameraActive = false;
    
    private View mFlashOptionView;
    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		vuforiaAppSession = new SampleApplicationSession(this);
		startLoadingAnimation();
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		initApplicationAR();
		//mTextures = new Vector<Texture>();
	    //loadTextures();
		
	}

	private void startLoadingAnimation() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null, false);
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
	
	// Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new GameRenderer(this, vuforiaAppSession);
        //mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
    }
    
    
	@Override
	public boolean doInitTrackers() {
		boolean result = true;
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker trackerBase = trackerManager.initTracker(MarkerTracker.getClassType());
        MarkerTracker markerTracker = (MarkerTracker) (trackerBase);
        
        if (markerTracker == null) {
            Log.e(LOGTAG, "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        
        return result;
	}

	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager.getTracker(MarkerTracker.getClassType());
        if (markerTracker == null) return false;
        return true;
	}

	@Override
	public boolean doStartTrackers() {
		boolean result = true;
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager.getTracker(MarkerTracker.getClassType());
        if (markerTracker != null) markerTracker.start();
        return result;
	}

	@Override
	public boolean doStopTrackers() {
		boolean result = true;
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager.getTracker(MarkerTracker.getClassType());
        if (markerTracker != null) markerTracker.stop();
        return result;
	}

	@Override
	public boolean doUnloadTrackersData() {
		boolean result = true;
        return result;
	}

	@Override
	public boolean doDeinitTrackers() {
		boolean result = true;
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(MarkerTracker.getClassType());
        return result;
	}

	@Override
	public void onInitARDone(SampleApplicationException e) {
		if (e == null) {
			
            initApplicationAR();
            mRenderer.mIsActive = true;
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mUILayout.bringToFront();
            
            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e1) {
            	Log.e(LOGTAG, e1.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            if (result) mContAutofocus = true;
            
        } else {
        	Log.e(LOGTAG, e.getString());
            finish();
        }
		
	}

	@Override
	public void onQCARUpdate(State state) {
		// TODO Auto-generated method stub
		
	}

}
