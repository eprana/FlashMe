package com.imac.FlashMe;

import java.util.List;
import java.util.Vector;

import com.imac.VuforiaApp.SampleApplicationControl;
import com.imac.VuforiaApp.SampleApplicationException;
import com.imac.VuforiaApp.SampleApplicationSession;
import com.imac.VuforiaApp.utils.LoadingDialogHandler;
import com.imac.VuforiaApp.utils.SampleApplicationGLView;
import com.imac.VuforiaApp.utils.Texture;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity  extends Activity implements SampleApplicationControl {
	
	private static final String LOGTAG = "GameActivity";
	
	private ParseUser currentUser;
	private ParseObject newPlayer;
	
	private final Context context = this;
	private LayoutInflater inflater;
	private String gameName;
	private View mainView;
	private TextView time;
	private TextView life;
	private TextView munitions;
	private int minutes;
	
	SampleApplicationSession vuforiaAppSession;
	private SampleApplicationGLView mGlView;
	private GameRenderer mRenderer;
	
	private Vector<Texture> mTextures;
    
    private Marker dataSet[];
    
//	private boolean mFlash = false;
	private boolean mContAutofocus = false;
//	private boolean mIsFrontCameraActive = false;
//	private View mFlashOptionView;
    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    private Handler handler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Get game name passed in extras
		Intent intent = getIntent();
		gameName = intent.getStringExtra("GAME");
		
		// Get layout elements
		inflater = LayoutInflater.from(context);
		mainView = inflater.inflate(R.layout.activity_game, null, false);
		time = (TextView) mainView.findViewById(R.id.text_time);
		life = (TextView) mainView.findViewById(R.id.text_life);
		munitions = (TextView) mainView.findViewById(R.id.text_munitions);
		
		initLayoutValues();
		
		currentUser = ParseUser.getCurrentUser();
	 	//State 0:offline, 1:online
	 	currentUser.put("state", 1);
	 	currentUser.saveInBackground();
		
		//mTextures = new Vector<Texture>();
	    //loadTextures();
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	 	currentUser.put("state", 0);
	 	currentUser.saveInBackground();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	 	currentUser.put("state", 1);
	 	currentUser.saveInBackground();
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(gameName);
		alertDialog.setMessage("Are you sure you want to leave this game ?");
		alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User wants to log out
				finish();
			}
		});
		alertDialog.setNegativeButton("CANCEL", null);
		alertDialog.create();
		alertDialog.show();	
	}
	private void initLayoutValues() {
		
		// Create player for the game
		newPlayer = new ParseObject("Player");
		newPlayer.put("state", 0);
		newPlayer.put("life", 50);
		newPlayer.put("munitions", 500);
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("name", gameName);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			
			@Override
			public void done(ParseObject game, ParseException e) {
				newPlayer.put("game", game);
				game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
					
					@Override
					public void done(List<ParseObject> teams, ParseException e) {
						for(final ParseObject team: teams) {
							team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> players, ParseException e) {
									for(ParseObject player : players) {
										if(currentUser.getUsername().equals(player.getString("username"))) {
											newPlayer.put("team", team);
											newPlayer.saveInBackground( new SaveCallback() {
												
												@Override
												public void done(ParseException e) {
													waitingForPlayers();
												}
											});
											return;
										}
									}
								}
							});
						}
					}
				});
			}
		});
	}
	
	private void waitingForPlayers() {
		life.setText(String.valueOf(newPlayer.getInt("life")));
		munitions.setText(String.valueOf(newPlayer.getInt("munitions")));
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(gameName);
		alertDialog.setMessage("Waiting for other players to be ready...");
		alertDialog.setPositiveButton("START", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				// Init Vuforia
				vuforiaAppSession = new SampleApplicationSession(GameActivity.this);
				startLoadingAnimation();
				vuforiaAppSession.initAR(GameActivity.this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				initApplicationAR();
				
				initTimer();
			}
		});
		alertDialog.create();
		alertDialog.show();
	}
	
	private void initTimer() {
		minutes = 10;
		new CountDownTimer(minutes*60000, 1000) {
			public void onTick(long millisUntilFinished) {
				int minutesRemaining = (int) Math.floor((millisUntilFinished/1000)/60);
				int secondsRemaining = (int) ((millisUntilFinished/1000) - (minutesRemaining*60));
				time.setText(minutesRemaining+":"+secondsRemaining);
		     }

		     public void onFinish() {
		         time.setText("GAME OVER");
		     }
		  }.start();
	}
	
	@Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
        newPlayer.deleteInBackground();
        super.onDestroy();
//        mTextures.clear();
//        mTextures = null;
        System.gc();
    }
	
	public void marqueurEnVue(final int id) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                    	Toast.makeText(GameActivity.this, "Marqueur "+id+" en vue", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

	private void startLoadingAnimation() {

        //mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null, false);
        mainView.setVisibility(View.VISIBLE);
        mainView.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mainView.findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mainView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
	
	// Initializes AR application components.
    private void initApplicationAR() {
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
        
        dataSet = new Marker[2];
	        
        dataSet[0] = markerTracker.createFrameMarker(0, "Zizi", new Vec2F(50, 50));
        if (dataSet[0] == null) {
            Log.e(LOGTAG, "Failed to create frame marker MarkerANous.");
            return false;
        }
        dataSet[1] = markerTracker.createFrameMarker(1, "Xopi", new Vec2F(50, 50));
        if (dataSet[1] == null) {
            Log.e(LOGTAG, "Failed to create frame marker MarkerANous.");
            return false;
        }
        
        Log.i(LOGTAG, "Successfully initialized MarkerTracker.");
        
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
            mainView.bringToFront();
            
            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            
            // Sets the layout background to transparent
            mainView.setBackgroundColor(Color.TRANSPARENT);
            
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
