package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.GpsStatus.Listener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity  extends Activity implements SampleApplicationControl {

	private static final String LOGTAG = "GameActivity";

	private ParseUser currentUser;
	private String currentUserTeam;
	private String gameId;
	private String gameName;
	private boolean isCreator;
	private int lastMarkerId;
	private int globalGunId;

	private Firebase appRef;
	private Firebase gameRef;
	private float nbPlayersReady;

	private int bestTeamScore = -1000;
	private String bestTeamId = "TOTO";
	private String bestTeamName;
	private int playerScore = 0;

	private int finalCount = 0;
	private final Context context = this;
	private LayoutInflater inflater;
	private ArrayList<String> teamsId = new ArrayList<String>();
	private ArrayList<Integer> markerId = new ArrayList<Integer>();
	private HashMap<String, ArrayList<String>> teamIdToPlayerIdArray = new HashMap<String, ArrayList<String>>();
	private HashMap<String, Integer> teamIdToTeamScore = new HashMap<String,Integer>();
	private HashMap<Integer, String> markerIdToPlayerId = new HashMap<Integer, String>();
	private View mainView;
	private ImageView gauge;
	private TextView time;
	private TextView life;
	private int gun;
	private int tmpLostMunitions;
	private TextView munitions;
	private ImageView munitionsIcon;
	private int minutes;
	private ProgressDialog waitingDialog;
	
	// Vibrator and sounds
	private Vibrator vibrator;
	private SoundPool soundPool;
	private int gunSoundID;
	private int chainsawSoundID;
	private int scourgeSoundID;
	private int munitionsSoundID;
	private int pointsSoundID;
	private int deathSoundID;
	boolean loaded = false;
	
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
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d("Zizanie", "DEBUG : Create GameActivity");

		mTextures = new Vector<Texture>();
		loadTextures();

		// Get game name passed in extras
		Intent intent = getIntent();
		gameId = intent.getStringExtra("GAME_ID");
		minutes = intent.getIntExtra("MINUTES", 20);

		// Get layout elements
		inflater = LayoutInflater.from(context);
		mainView = inflater.inflate(R.layout.activity_game, null, false);
		gauge = (ImageView) mainView.findViewById(R.id.gauge_value);
		time = (TextView) mainView.findViewById(R.id.text_time);
		life = (TextView) mainView.findViewById(R.id.text_life);
		munitions = (TextView) mainView.findViewById(R.id.text_munitions);
		munitionsIcon = (ImageView) mainView.findViewById(R.id.ic_munitions);

		// Parse - currentUser
		currentUser = ParseUser.getCurrentUser();
		//State 0:offline, 1:online
		currentUser.put("state", 1);
		currentUser.saveInBackground();

		// Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				loaded = true;
			}
		});
		gunSoundID = soundPool.load(this, R.raw.gun, 1);
		chainsawSoundID = soundPool.load(this, R.raw.chainsaw, 1);
		scourgeSoundID = soundPool.load(this, R.raw.scourge, 1);
		munitionsSoundID = soundPool.load(this, R.raw.munitions, 1);
		pointsSoundID = soundPool.load(this, R.raw.points, 1);
		deathSoundID = soundPool.load(this, R.raw.death, 1);
		
		lastMarkerId = -1;

		initGame();
	}

	// Initialize game
	private void initGame() {
		// Get game
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
		query.whereEqualTo("objectId", gameId);
		query.getFirstInBackground(new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject game, ParseException e) {
				gameName = game.getString("name");
				game.saveInBackground();
				try {
					isCreator = game.getParseUser("createdBy").fetchIfNeeded().getUsername().equals(currentUser.getUsername()) ? true : false;
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				// Get teams
				game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> teams, ParseException e) {

						// For each team
						Iterator<ParseObject> it = teams.iterator();
						while(it.hasNext()) {
							final ArrayList<String> playerArray = new ArrayList<String>();
							final ParseObject team = it.next();
							teamsId.add(team.getObjectId());
							Log.d("Zizanie", "DEBUG : " + team.getObjectId());
							team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
								@Override
								public void done(List<ParseObject> players, ParseException e) {

									// For each player
									Iterator<ParseObject> it = players.iterator();
									while(it.hasNext()) {
										ParseObject player = it.next();
										// Add marker
										markerIdToPlayerId.put(player.getInt("markerId"), player.getObjectId());
										playerArray.add(player.getObjectId());

										if(player.getObjectId().equals(currentUser.getObjectId())) {
											currentUserTeam = team.getObjectId();
											initFirebase();
										}
									}
									teamIdToPlayerIdArray.put(team.getObjectId(), playerArray);
								}
							});
						}
					}
				});
			}
		});
	}

	private void loadTextures() {

		mTextures.add(Texture.loadTextureFromApk("Texture/gun.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/chainsaw.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/scourge.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/munitions.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/points.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/poison.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/pink_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/orange_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/green_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/cyan_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/purple_logo.png", getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/red_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/blue_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/black_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/white_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/grey_logo.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("Texture/yellow_logo.png",getAssets()));
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
				// User wants to end game
				if(isCreator) {
					gameRef.removeValue();
					gameRef = null;
				}
				finish();
			}
		});
		alertDialog.setNegativeButton("CANCEL", null);
		alertDialog.create();
		alertDialog.show();	
	}

	private void initFirebase() {

		appRef = new Firebase("https://flashme.firebaseio.com/");
		gameRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId);

		// Create connection
		final Firebase gameConnectionsRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/connections");
		final Firebase connectedRef = new Firebase("https://flashme.firebaseio.com/.info/connected");
		connectedRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				boolean isConnected = snapshot.getValue(Boolean.class);
				if (isConnected) {
					Firebase presence = gameConnectionsRef.push();
					presence.setValue(Boolean.TRUE);
					presence.onDisconnect().removeValue();
					Log.d(LOGTAG, "Connected !");
				}
			}

			@Override
			public void onCancelled(FirebaseError e) {
				Log.d(LOGTAG, e.getMessage()+" : Listener was cancelled at .info/connected");
			}
		});

		// Create simple login
		SimpleLogin authClient = new SimpleLogin(appRef);
		authClient.loginAnonymously(new SimpleLoginAuthenticatedHandler() {
			@Override
			public void authenticated(com.firebase.simplelogin.enums.Error e, User u) {
				if(e != null) {
					Log.d(LOGTAG, "Error while logging in");
				}
				else {
					Log.d(LOGTAG, "User logged in !");
				}				
			}
		});

		// Create team
		Firebase teamRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam);
		Firebase scoreTeamRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/teamScore");
		scoreTeamRef.setValue(0);


		// Create user
		Firebase userRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/user/"+currentUser.getObjectId());
		Map<String, Object> userData = new HashMap<String, Object>();
		userData.put("life", 0);
		userData.put("gun", 0);
		userData.put("munitions", 500);
		userRef.setValue(userData);

		life.setText("0");
		gun = 0;
		munitions.setText("500");

		// Add listener
		userRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onCancelled(FirebaseError e) {
				Log.d(LOGTAG, e.getMessage());
			}

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Object value = snapshot.getValue();
				if (value == null) {
					Log.d(LOGTAG, "User doesn't exist");
				} else {
					String lifeValue = ((Map)value).get("life").toString();
					String munitionsValue = ((Map)value).get("munitions").toString();
					int gunId = Integer.parseInt(((Map)value).get("gun").toString());
					life.setText(lifeValue);
					munitions.setText(munitionsValue);
					gun = gunId;
				}
			}

		});

		waitingForPlayers();
	}

	private void updateNbPlayers() {
		final Firebase presenceRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/connections");
		presenceRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onCancelled(FirebaseError e) {
				Log.d(LOGTAG, e.getMessage());
			}

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Log.d(LOGTAG, "NB PLAYERS : "+snapshot.getChildrenCount());
				nbPlayersReady = snapshot.getChildrenCount();
				//if(nbPlayersReady == markerIdToPlayerId.size()) {
				if(nbPlayersReady == 1) {
					waitingDialog.dismiss();
					initTimer();

					// Init Vuforia
					vuforiaAppSession = new SampleApplicationSession(GameActivity.this);
					startLoadingAnimation();
					vuforiaAppSession.initAR(GameActivity.this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					initApplicationAR();
				}
			}
		});
	}

	private void waitingForPlayers() {
		waitingDialog = ProgressDialog.show(context, gameName, "Waiting for other players to be ready...", true);
		waitingDialog.show();
		updateNbPlayers();
	}

	private void computeScore() {
		final int currentScore = currentUser.getInt("totalScore");

		// Score = life
		String userURL = "https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/user/"+currentUser.getObjectId();
		Firebase userRef = new Firebase(userURL);
		userRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Object value = snapshot.getValue();
				if (value == null) {
					Log.d(LOGTAG, "User doesn't exist");
				} else {

					long life = ((Long) ((Map)value).get("life"));
					playerScore = (int) life;

					currentUser.put("totalScore",  currentScore + (int)life);

					// Best score
					if(life > currentUser.getInt("bestScore")) {
						currentUser.put("bestScore", life);
					}
				}	

				currentUser.saveInBackground();
			}

			@Override
			public void onCancelled(FirebaseError arg0) {
				System.err.println("Listener was cancelled");

			}
		});
	}

	private void displayScores() {
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.scores, null);

		final LinearLayout ll = (LinearLayout) promptsView.findViewById(R.id.score_layout);

		TextView t_yourScore = new TextView(context);
		t_yourScore.setText("Your score : " + playerScore);
		t_yourScore.setTextSize(20);
		ll.addView(t_yourScore);

//		TextView t_winner = new TextView(context);
//		t_winner.setText("Winning team  : " + bestTeam);
//		t_winner.setTextSize(20);
//		ll.addView(t_winner);

		
		for(final String teamId : teamsId) {

			// Get parse team
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Team");
			query.whereEqualTo("objectId", teamId);
			query.getFirstInBackground(new GetCallback<ParseObject>(){
				@Override
				public void done(ParseObject team, ParseException e) {
					String teamName = team.getString("name");
					if(teamId.equals(bestTeamId)) {
						bestTeamName = teamName;					
					}
					TextView t_team = new TextView(context);
					String teamScore;
					if( teamIdToTeamScore.get(teamId) == null) {
						teamScore = "Team did not play";
					}
					else {
						teamScore =  teamIdToTeamScore.get(teamId).toString();
					}

					t_team.setText(teamName + " score : " + teamScore);
					t_team.setTextSize(20);
					ll.addView(t_team);

				}

			});
		}
		
//		TextView t_winner = new TextView(context);
//		t_winner.setText("Winning team  : " + bestTeamName);
//		t_winner.setTextSize(20);
//		ll.addView(t_winner);	


		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);
		alertDialogBuilder.setTitle("Score table");

		alertDialogBuilder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Delete Firebase
				if(isCreator) {
					gameRef.removeValue();
					gameRef = null;
				}

				finish();

			}
		});
		alertDialogBuilder.create();
		alertDialogBuilder.show();
	}

	private void doPlayerWin() {
		Iterator<Entry<String, ArrayList<String>>> it = teamIdToPlayerIdArray.entrySet().iterator();	
		
		// For each team
		while(it.hasNext()) {
			final String teamId = it.next().getKey();

			String teamScoreURL = "https://flashme.firebaseio.com/game/"+gameId+"/team/"+teamId;
			Firebase teamScoreRef = new Firebase(teamScoreURL);

			teamScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					Object value = snapshot.getValue();
					if (value == null) {
						Log.d(LOGTAG, "User doesn't exist");
					} 
					else {

						long teamScore = ((Long) ((Map)value).get("teamScore"));
						teamIdToTeamScore.put(teamId, (int)teamScore);
						if(teamScore > bestTeamScore) {
							bestTeamScore = (int) teamScore;
							bestTeamId = teamId;
						}

						finalCount++;

						if(finalCount == 1 /*teamIdToPlayerIdArray.size()*/) {
							if(bestTeamId.equals(currentUserTeam)) {
								currentUser.increment("victories");
							}
							else {
								currentUser.increment("defeats");
							}
							currentUser.saveInBackground();

							displayScores();

						}
					}	
				}

				@Override
				public void onCancelled(FirebaseError arg0) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	private void updateGameState() {
		if(isCreator) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
			query.whereEqualTo("objectId", gameId);
			query.getFirstInBackground(new GetCallback<ParseObject>(){
				@Override
				public void done(ParseObject game, ParseException e) {

					game.put("state", 1);
					game.saveInBackground();
				}
			});
		}	
	}
	
	private void gameListening() {
		gameRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onCancelled(FirebaseError e) {
				Log.d(LOGTAG, e.getMessage());				
			}

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Object value = snapshot.getValue();
				if (value == null) {
					Log.d(LOGTAG, "Game doesn't exist");
				} else {
					String timeValue = (String)((Map)value).get("timer");

					if(timeValue != null) {
						time.setText(timeValue);

						// End of a game
						if(timeValue.equals("0:1")) {
							Log.d("Zizanie", "Game is FINISHED ! ");
							
							// End Vuforia
							try {
								vuforiaAppSession.stopAR();
							} catch (SampleApplicationException e) {
								Log.e(LOGTAG, e.getString());
							}

							// Scores
							computeScore();
							doPlayerWin();

							// Game state
							updateGameState();
							
							

							// Rank
							/*if(isCreator) {
								Log.d("Zizanie", "RANK");
								ParseQuery<ParseUser> query = ParseUser.getQuery();
								query.orderByDescending("totalScore");						
								query.findInBackground(new FindCallback<ParseUser>() {

									@Override
									public void done(List<ParseUser> userlist, ParseException e) {
										if (e == null) {
											Log.d("Zizanie", "Size : " + Integer.toString(userlist.size()));
											Iterator<ParseUser> it = userlist.iterator();
											int i = 1;
											while(it.hasNext()) {
												ParseUser user = it.next();
												Log.d("Zizanie", "User : " + user.toString());
												Log.d("Zizanie", "Rank : " + Integer.toString(i));
												user.put("rank", i);
												user.put("tamere", 28);
												user.saveInBackground();
												++i;

											}

										} else {
											Log.d("Zizanie", "EXCEPTION");
											// handle Parse Exception here
										}
									}
								});
							}*/	
						}
					}
				}	

			}
		});
	}

	private void initTimer() {

		// Creator handle timer
		if(isCreator) {
			new CountDownTimer(15000 /*minutes*60000*/, 1000) {
				public void onTick(long millisUntilFinished) {
					int minutesRemaining = (int) Math.floor((millisUntilFinished/1000)/60);
					int secondsRemaining = (int) ((millisUntilFinished/1000) - (minutesRemaining*60));
					if(gameRef != null) {
						gameRef.child("timer").setValue(minutesRemaining+":"+secondsRemaining);
					}

				}

				public void onFinish() {

				}
			}.start();
		}

		gameListening();
	}

	@Override
	protected void onDestroy() {
		Log.d(LOGTAG, "onDestroy");
//		try {
//			vuforiaAppSession.stopAR();
//		} catch (SampleApplicationException e) {
//			Log.e(LOGTAG, e.getString());
//		}
		super.onDestroy();
		mTextures.clear();
		mTextures = null;

		System.gc();
	}

	public boolean isEnnemy(String ennemyId) {
		for(String playerId : teamIdToPlayerIdArray.get(currentUserTeam)) {
			if(playerId.equals(ennemyId)) {
				return false;
			}

		}
		return true;
	}

	public void updateGauge(final int markerId, final String playerId) {

		Log.d(LOGTAG, "Marker " + markerId + " detected from " + playerId);
		// Set gauge value according to device resolution
		if(Integer.parseInt(munitions.getText().toString()) <= 0){
			Log.d(LOGTAG, "You're out of munition !");
			return;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		int maxValue = (int) (300 * scale + 0.5f);
		int incrementValue = maxValue/10;
		// Flashing same marker
		if(lastMarkerId == markerId) {
			// Fill the gauge
			if(gauge.getLayoutParams().height < maxValue){
				// Gauge not full
				gauge.getLayoutParams().height += incrementValue;
			}
			else {
				// Gauge full
				 vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				 vibrator.vibrate(300);

				 AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				 float actualVolume = (float) audioManager
				 		.getStreamVolume(AudioManager.STREAM_MUSIC);
				 float maxVolume = (float) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    	 float volume = actualVolume / maxVolume;

		    	  //Is the sound loaded already?
				 if (loaded) {
					 switch(globalGunId){
						 case 0:
							 //gun
							 soundPool.play(gunSoundID, volume, volume, 1, 0, 1f);
							 break;
						 case 1:
							 //chain saw
							 soundPool.play(chainsawSoundID, volume, volume, 1, 0, 1f);
							 break;
						 case 2:
							 //scourge
							 soundPool.play(scourgeSoundID, volume, volume, 1, 0, 1f);
							 break;
						 default:
							 break;
					 }
					
				 }
				gauge.getLayoutParams().height = 0;
				int plus = 1;
				if(!isEnnemy(playerId)) {
					plus = -1;
				}
				if(!currentUser.getObjectId().equals(playerId)) {
					updatePoints(playerId, -2*(gun+1));
				}
				updatePoints(currentUser.getObjectId(), plus*2*(gun+1));

				updateMunitions(-2+gun);
			}
		}

		// Flashing new marker
		else {
			gauge.getLayoutParams().height = 0;
			tmpLostMunitions = 0;
		}
		lastMarkerId = markerId;
	}

	public void updateMunitions(final int nbMunitions) {
		Firebase munitionsRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/user/"+currentUser.getObjectId()+"/munitions");
		munitionsRef.runTransaction(new Transaction.Handler() {
			@Override
			public Transaction.Result doTransaction(MutableData currentData) {
				int currentMunitions = currentData.getValue(Integer.class);
				currentData.setValue(currentMunitions + nbMunitions);

				if(nbMunitions > 0) {
					vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					 vibrator.vibrate(300);
	
					 AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
					 float actualVolume = (float) audioManager
					 		.getStreamVolume(AudioManager.STREAM_MUSIC);
					 float maxVolume = (float) audioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			    	 float volume = actualVolume / maxVolume;
			    	 
			    	 soundPool.play(munitionsSoundID, volume, volume, 1, 0, 1f);
				}
				
				return Transaction.success(currentData);
			}

			@Override
			public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
				if (e != null) {
					Log.d(LOGTAG, e.getMessage());
				} else {
					if (!committed) {
						Log.d(LOGTAG, "Transaction not comitted");
					} else {						
						Log.d(LOGTAG, "Transaction succeeded");
					}
				}
			}
		});
	}

	public void updateGun(final int gunId) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() { // This thread runs in the UI
					@Override
					public void run() {
						 vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						 vibrator.vibrate(300);
						switch(gunId) {
						case 0:
							munitionsIcon.setImageResource(R.drawable.ic_munitions);
							globalGunId = 0;
							break;
						case 1:
							munitionsIcon.setImageResource(R.drawable.chainsaw);
							globalGunId = 1;
							break;
						case 2:
							munitionsIcon.setImageResource(R.drawable.scourge);
							globalGunId = 2;
							break;
						}
					}
				});
			}
		};
		new Thread(runnable).start();

		Firebase gunRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/user/"+currentUser.getObjectId()+"/gun");
		gunRef.runTransaction(new Transaction.Handler() {
			@Override
			public Transaction.Result doTransaction(MutableData currentData) {
				currentData.setValue(gunId);
				return Transaction.success(currentData);
			}

			@Override
			public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
				if (e != null) {
					Log.d(LOGTAG, e.getMessage());
				} else {
					if (!committed) {
						Log.d(LOGTAG, "Transaction not comitted");
					} else {
						Log.d(LOGTAG, "Transaction succeeded");
					}
				}
			}
		});
	}

	public void updateCurrentUserPoints(int points) {
		updatePoints(currentUser.getObjectId(), points);
		
		 vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		 vibrator.vibrate(300);

		 AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		 float actualVolume = (float) audioManager
		 		.getStreamVolume(AudioManager.STREAM_MUSIC);
		 float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		 float volume = actualVolume / maxVolume;
		
		if(points > 0) {	    	 
			 vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 vibrator.vibrate(300);
	    	 soundPool.play(pointsSoundID, volume, volume, 1, 0, 1f);
		}else {
			 vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 vibrator.vibrate(300);
			 soundPool.play(deathSoundID, volume, volume, 1, 0, 1f);
		}
		
	}

	private void updatePoints(String playerId, final int points) {

		Iterator<Entry<String, ArrayList<String>>> it = teamIdToPlayerIdArray.entrySet().iterator();

		// For each team
		while(it.hasNext()) {

			String teamId = it.next().getKey();
			ArrayList<String> playerArray = (ArrayList<String>)teamIdToPlayerIdArray.get(teamId);

			if(playerArray != null) {

				// For each player in playerArray
				for(String player : playerArray) {
					if(player.equals(playerId)) {

						// Update user score
						Firebase lifeRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+teamId+"/user/"+playerId+"/life");
						lifeRef.runTransaction(new Transaction.Handler() {
							@Override
							public Transaction.Result doTransaction(MutableData currentData) {
								int currentLife = currentData.getValue(Integer.class);
								currentData.setValue(currentLife + points);

								return Transaction.success(currentData);
							}

							@Override
							public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
								if (e != null) {
									Log.d(LOGTAG, e.getMessage());
								} else {
									if (!committed) {
										Log.d(LOGTAG, "Transaction not comitted");
									} else {
										Log.d(LOGTAG, "Transaction succeeded");
									}
								}
							}
						});


						// Update team score
						Firebase teamScoreRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+teamId+"/teamScore");
						teamScoreRef.runTransaction(new Transaction.Handler() {
							@Override
							public Transaction.Result doTransaction(MutableData currentData) {
								int currentScore = currentData.getValue(Integer.class);
								currentData.setValue(currentScore + points);

								return Transaction.success(currentData);
							}

							@Override
							public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
								if (e != null) {
									Log.d(LOGTAG, e.getMessage());
								} else {
									if (!committed) {
										Log.d(LOGTAG, "Transaction not comitted");
									} else {
										Log.d(LOGTAG, "Transaction succeeded");
									}
								}
							}
						});

					}
				}
			}
		}
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
		mRenderer.setTextures(mTextures);
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
		final MarkerTracker markerTracker = (MarkerTracker) tManager.getTracker(MarkerTracker.getClassType());
		if (markerTracker == null) {
			return false;
		}

		Marker[] dataSet = new Marker[markerIdToPlayerId.size() + 6];
		//Marker[] dataSet = new Marker[markerIdToPlayerId.size()];

		int i = 0;
		for (Entry<Integer, String> entry : markerIdToPlayerId.entrySet()) {								    
			dataSet[i] = markerTracker.createFrameMarker(entry.getKey(), entry.getValue() , new Vec2F(50, 50));
			if (dataSet[i] == null) {
				Log.e(LOGTAG, "Failed to create frame marker." + entry.getKey());
			}
			++i;
		}
		int index = 506;
		for(int j = dataSet.length-6; j <dataSet.length; ++j) {
			dataSet[j] = markerTracker.createFrameMarker(index, "index" + index , new Vec2F(50, 50));
			++index;
		}

		for(int k = 0; k<dataSet.length; ++k){
			Log.d("index last marqueurs ", "#############" + dataSet[k]);
		}

		GameActivity.this.dataSet = dataSet;

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
	public void onConfigurationChanged(Configuration config) {
		if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			Log.d(LOGTAG, "Enter LANDSCAPE mode");
		}
		if(config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Log.d(LOGTAG, "Enter PORTRAIT mode");
		}
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
