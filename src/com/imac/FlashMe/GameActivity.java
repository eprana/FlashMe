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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
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

	private Firebase appRef;
	private Firebase gameRef;
	private float nbPlayersReady;

	private int bestTeamScore = -1000;
	private String bestTeam = "";

	private final Context context = this;
	private LayoutInflater inflater;
	private ArrayList<String> teamsId = new ArrayList<String>();
	private ArrayList<Integer> markerId = new ArrayList<Integer>();
	private HashMap<String, ArrayList<String>> teamIdToPlayerIdArray = new HashMap<String, ArrayList<String>>();
	private HashMap<Integer, String> markerIdToPlayerId = new HashMap<Integer, String>();
	private View mainView;
	private ImageView gauge;
	private TextView time;
	private TextView life;
	private int gun;
	private int tmpLostMunitions;
	private TextView munitions;
	private int minutes;
	private ProgressDialog waitingDialog;

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

		// Parse - currentUser
		currentUser = ParseUser.getCurrentUser();
		//State 0:offline, 1:online
		currentUser.put("state", 1);
		currentUser.saveInBackground();

		lastMarkerId = -1;

		// Add pictograms objects in the map

		// Get game
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
		query.whereEqualTo("objectId", gameId);
		query.getFirstInBackground(new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject game, ParseException e) {
				gameName = game.getString("name");
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
										Log.d("Zizanie", "Add player to array : " + player.getObjectId());
										if(player.getObjectId().equals(currentUser.getObjectId())) {
											currentUserTeam = team.getObjectId();
											createFirebaseUser();
										}
									}
									teamIdToPlayerIdArray.put(team.getObjectId(), playerArray);

									for(String player : playerArray) {
										Log.d("Zizanie", "In my array : " + player);
									}
								}
							});

						}
						//createFirebaseUser();
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
				finish();
			}
		});
		alertDialog.setNegativeButton("CANCEL", null);
		alertDialog.create();
		alertDialog.show();	
	}

	private void createFirebaseUser() {

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
		userData.put("life", 50);
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
				if(nbPlayersReady == 2 /*markerId.size()*/) {
					waitingDialog.dismiss();
					if(isCreator) {
						initTimer();
					}

				}
			}
		});
	}
	private void waitingForPlayers() {
		waitingDialog = ProgressDialog.show(context, gameName, "Waiting for other players to be ready...", true);
		waitingDialog.show();
		updateNbPlayers();

		// Init Vuforia
		vuforiaAppSession = new SampleApplicationSession(GameActivity.this);
		startLoadingAnimation();
		vuforiaAppSession.initAR(GameActivity.this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		initApplicationAR();
	}

	private void initTimer() {
		if(isCreator) {
			new CountDownTimer(1*60000, 1000) {
				public void onTick(long millisUntilFinished) {
					int minutesRemaining = (int) Math.floor((millisUntilFinished/1000)/60);
					int secondsRemaining = (int) ((millisUntilFinished/1000) - (minutesRemaining*60));
					gameRef.child("timer").setValue(minutesRemaining+":"+secondsRemaining);
				}

				public void onFinish() {

					//					// Update Parse	
					//					final int currentScore = currentUser.getInt("totalScore");
					//
					//					// Score = life
					//					String userURL = "https://flashme.firebaseio.com/game/"+gameId+"/team/"+currentUserTeam+"/user/"+currentUser.getObjectId();
					//					Firebase userRef = new Firebase(userURL);
					//					userRef.addValueEventListener(new ValueEventListener() {
					//
					//						@Override
					//						public void onDataChange(DataSnapshot snapshot) {
					//							Object value = snapshot.getValue();
					//							if (value == null) {
					//								Log.d(LOGTAG, "User doesn't exist");
					//							} else {
					//
					//								long life = ((Long) ((Map)value).get("life"));
					//								currentUser.put("totalScore", currentScore + (int)life);
					//
					//								// Best score
					//								if(life > currentUser.getInt("bestScore")) {
					//									currentUser.put("bestScore", life);
					//								}
					//							}			
					//						}
					//
					//						@Override
					//						public void onCancelled(FirebaseError arg0) {
					//							System.err.println("Listener was cancelled");
					//
					//						}
					//					});
					//
					//					
					//
					//					// Victories - Defeats
					//					
					//					Iterator<Entry<String, ArrayList<String>>> it = teamIdToPlayerIdArray.entrySet().iterator();	
					//					// For each team
					//					while(it.hasNext()) {
					//					    final String teamId = it.next().getKey();
					//					    
					//					    String teamScoreURL = "https://flashme.firebaseio.com/game/"+gameId+"/team/"+teamId;
					//						Firebase teamScoreRef = new Firebase(teamScoreURL);
					//						
					//						teamScoreRef.addValueEventListener(new ValueEventListener() {
					//
					//							@Override
					//							public void onDataChange(DataSnapshot snapshot) {
					//								Object value = snapshot.getValue();
					//								if (value == null) {
					//									Log.d(LOGTAG, "User doesn't exist");
					//								} else {
					//
					//									long teamScore = ((Long) ((Map)value).get("teamScore"));
					//									if(teamScore > bestTeamScore) {
					//										bestTeamScore = (int) teamScore;
					//										bestTeam = teamId;
					//									}
					//								}			
					//							}
					//
					//							@Override
					//							public void onCancelled(FirebaseError arg0) {
					//								System.err.println("Listener was cancelled");
					//
					//							}
					//						});
					//						
					//					}
					//					
					//					if(bestTeam.equals(currentUserTeam)) {
					//						currentUser.increment("victories");
					//						Log.d("Zizanie", "YOU WON ! ");
					//					}
					//					else {
					//						currentUser.increment("defeats");
					//						Log.d("Zizanie", "YOU LOST NOOB ! ");
					//					}
					//					currentUser.saveInBackground();
					//					
					//					
					//					
					//					// Rank
					//					
					//					// Delete Firebase
					//					Firebase gameRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId);
					//					gameRef.removeValue();
					//					
					//
					//					time.setText("GAME OVER");

				}
			}.start();
		}
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
					time.setText(timeValue);

					if(timeValue.equals("0:0")) {

						// Update Parse	
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
									currentUser.put("totalScore", currentScore + (int)life);

									// Best score
									if(life > currentUser.getInt("bestScore")) {
										currentUser.put("bestScore", life);
									}
								}			
							}

							@Override
							public void onCancelled(FirebaseError arg0) {
								System.err.println("Listener was cancelled");

							}
						});



						// Victories - Defeats

						Iterator<Entry<String, ArrayList<String>>> it = teamIdToPlayerIdArray.entrySet().iterator();	
						// For each team
						while(it.hasNext()) {
							final String teamId = it.next().getKey();

							String teamScoreURL = "https://flashme.firebaseio.com/game/"+gameId+"/team/"+teamId;
							Firebase teamScoreRef = new Firebase(teamScoreURL);

							teamScoreRef.addValueEventListener(new ValueEventListener() {

								@Override
								public void onDataChange(DataSnapshot snapshot) {
									Object value = snapshot.getValue();
									if (value == null) {
										Log.d(LOGTAG, "User doesn't exist");
									} else {

										long teamScore = ((Long) ((Map)value).get("teamScore"));
										if(teamScore > bestTeamScore) {
											bestTeamScore = (int) teamScore;
											bestTeam = teamId;
										}
									}			
								}

								@Override
								public void onCancelled(FirebaseError arg0) {
									System.err.println("Listener was cancelled");

								}
							});

						}

						if(bestTeam.equals(currentUserTeam)) {
							currentUser.increment("victories");
							Log.d("Zizanie", "YOU WON ! ");
							time.setText("YOUR TEAM WON !");
						}
						else {
							currentUser.increment("defeats");
							Log.d("Zizanie", "YOU LOST NOOB ! ");
							time.setText("YOU TEAM LOST");
						}
						currentUser.saveInBackground();



						// Rank

						// Delete Firebase
						if(isCreator) {
							Firebase gameRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId);
							gameRef.removeValue();
						}
						


						
					}
				}			
			}
		});
	}

	@Override
	protected void onDestroy() {
		Log.d(LOGTAG, "onDestroy");
		try {
			vuforiaAppSession.stopAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
		super.onDestroy();
		mTextures.clear();
		mTextures = null;

		System.gc();
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
				gauge.getLayoutParams().height = 0;
				updatePoints(currentUser.getObjectId(), 2*(gun+1));
				updatePoints(playerId, -2*(gun+1));
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

		//		// On boucle sur toues les équipes présentes dans le jeu
		//		for(String team : teamsId) {
		//			Firebase teamRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+team);
		//
		//			if(teamRef.child("/user/" + playerId) != null) {
		//
		//				Log.d("Zizanie", teamRef.child("/user/" + playerId).toString());
		//				Log.d("Zizanie", "Je suis dans la bonne team");
		//
		//				// Update user score
		//				Firebase lifeRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+team+"/user/"+playerId+"/life");
		//				lifeRef.runTransaction(new Transaction.Handler() {
		//					@Override
		//					public Transaction.Result doTransaction(MutableData currentData) {
		//						int currentLife = currentData.getValue(Integer.class);
		//						currentData.setValue(currentLife + points);
		//						Log.d("Zizanie", "User CurrentScore : " + currentLife);
		//						Log.d("Zizanie", "User Add : " + points);
		//						return Transaction.success(currentData);
		//					}
		//
		//					@Override
		//					public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
		//						if (e != null) {
		//							Log.d(LOGTAG, e.getMessage());
		//						} else {
		//							if (!committed) {
		//								Log.d(LOGTAG, "Transaction not comitted");
		//							} else {
		//								Log.d(LOGTAG, "Transaction succeeded");
		//							}
		//						}
		//					}
		//				});
		//
		//
		//				// Update team score
		//				Firebase teamScoreRef = new Firebase("https://flashme.firebaseio.com/game/"+gameId+"/team/"+team+"/teamScore");
		//				teamScoreRef.runTransaction(new Transaction.Handler() {
		//					@Override
		//					public Transaction.Result doTransaction(MutableData currentData) {
		//						int currentScore = currentData.getValue(Integer.class);
		//						currentData.setValue(currentScore + points);
		//						Log.d("Zizanie", "Team CurrentScore : " + currentScore);
		//						Log.d("Zizanie", "Team Add : " + points);
		//
		//						return Transaction.success(currentData);
		//					}
		//
		//					@Override
		//					public void onComplete(FirebaseError e, boolean committed, DataSnapshot currentData) {
		//						if (e != null) {
		//							Log.d(LOGTAG, e.getMessage());
		//						} else {
		//							if (!committed) {
		//								Log.d(LOGTAG, "Transaction not comitted");
		//							} else {
		//								Log.d(LOGTAG, "Transaction succeeded");
		//							}
		//						}
		//					}
		//				});
		//			}
		//}

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
