package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GameTeamsActivity extends ListActivity {

	private static final String LOGTAG = "TeamPlayersActivity";
	private Context context;
	private LayoutInflater inflater;
	
	// Data elements
	private static ParseUser currentUser = null;
	private boolean isCreator;
	private String gameId;
	private static List<String> teamsList = null;
	
	// Layout elements
	private TextView title;
	private AutoCompleteTextView autocompleteValue;
	private Button addButton;
	private GameTeamsParseAdapter gameTeamsParseAdapter;
	private ArrayAdapter<String> teamsAdapter;
	private static ProgressBar progress = null;
	private ImageButton refreshButton;
	private Button playButton;
	private ListView gameTeamsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		context = GameTeamsActivity.this;
		inflater = LayoutInflater.from(context);
		Intent intent = getIntent();
		gameId = intent.getStringExtra("GAME_ID");
		
		getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
		query.whereEqualTo("objectId", gameId);
		query.getFirstInBackground(new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject game, ParseException e) {
				try {
					isCreator = game.getParseUser("createdBy").fetchIfNeeded().getUsername().equals(currentUser.getUsername()) ? true : false;
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
		currentUser.put("state", 1);
		currentUser.saveInBackground();
		
		teamsList = new ArrayList<String>();
		
		autocompleteValue = (AutoCompleteTextView) this.findViewById(R.id.autocomplete_player);
		autocompleteValue.setHint("Team name");
		ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
		teamsQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> teams, ParseException e) {
				initAutoCompleteList(teams);
				autocompleteValue.setThreshold(1);
			}
		});
		title = (TextView) this.findViewById(R.id.activity_title);
		addButton = (Button) this.findViewById(R.id.add_player);
		progress = (ProgressBar) this.findViewById(R.id.progressBar);
		refreshButton = (ImageButton) this.findViewById(R.id.refresh_bt);
		playButton = (Button) this.findViewById(R.id.play);
		gameTeamsList = (ListView) this.findViewById(android.R.id.list);
		
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_autocompleteValue = autocompleteValue.getText().toString();
				int nbTeams = gameTeamsParseAdapter.getCount();
				if(s_autocompleteValue.equals("")){
					// Empty edit text
					Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_LONG).show();
					return;
				}
				else if(nbTeams > 3) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					alertDialog.setTitle(title.getText());
					alertDialog.setMessage("You can't add more than 4 teams in a game.");
					alertDialog.setPositiveButton("OK", null);
					alertDialog.create();
					alertDialog.show();
				}
				else {
					addTeamToGame(s_autocompleteValue);
				}
			}
		});
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameTeamsParseAdapter.loadObjects();
			}
		});
		
		// Play button
		playButton.setVisibility(View.VISIBLE);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start game
				int nbTeams = gameTeamsParseAdapter.getCount();
				if(!gameId.isEmpty()) {
					if(nbTeams < 2) {
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
						alertDialog.setTitle(title.getText());
						alertDialog.setMessage("You need to have at least 2 teams in the game if you want to play.");
						alertDialog.setPositiveButton("OK", null);
						alertDialog.create();
						alertDialog.show();
						return;
					}
					if(isCreator) {
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
						alertDialog.setTitle(title.getText());
						alertDialog.setMessage("How many minutes do you want this game to last ?");
						final NumberPicker np = new NumberPicker(context);
						np.setMaxValue(60);
						np.setMinValue(5);
						np.setValue(20);
						np.setWrapSelectorWheel(false);
						alertDialog.setView(np);
						alertDialog.setPositiveButton("START", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int id) {
								final Intent intent = new Intent(context, GameActivity.class);
								intent.putExtra("GAME_ID", gameId);
								intent.putExtra("MINUTES", np.getValue());
								startActivity(intent);
								
							}
						});
						alertDialog.setNegativeButton("CANCEL", null);
						alertDialog.create();
						alertDialog.show();
					}
					else {
						final Intent intent = new Intent(context, GameActivity.class);
						intent.putExtra("GAME_ID", gameId);
						startActivity(intent);
					}
				}
			}
		});
		
		initParseAdapter();
		
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
	
	private void initAutoCompleteList(List<ParseObject> teams) {
		for(ParseObject team: teams) {
			teamsList.add(team.getString("name"));
		}
		String[] teamsArray = new String[teamsList.size()];
		if(teamsArray != null) {
			teamsAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, teamsList.toArray(teamsArray));
			autocompleteValue.setAdapter(teamsAdapter);
		}
	}
	
	private void initParseAdapter() {
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("objectId", gameId);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject game, ParseException e) {
				gameTeamsParseAdapter = new GameTeamsParseAdapter(context, currentUser, game);
				title.setText(game.getString("name"));
				boolean enable = true;
				if(gameTeamsParseAdapter.isCreator()){
					// Display and enable autocomplete
					addButton.setBackgroundResource(R.drawable.dark_button);
				}
				else {
					// Display and disable autocomplete
					enable = false;
					addButton.setBackgroundResource(R.drawable.locked_button);
				}
				autocompleteValue.setEnabled(enable);
				addButton.setEnabled(enable);
				
				gameTeamsParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
					@Override
					public void onLoaded(List<ParseObject> arg0, Exception arg1) {
						gameTeamsList.setVisibility(View.VISIBLE);
						progress.setVisibility(View.INVISIBLE);
					}
					@Override
					public void onLoading() {
						progress.setVisibility(View.VISIBLE);
						gameTeamsList.setVisibility(View.VISIBLE);
					}
		    	});
				setListAdapter(gameTeamsParseAdapter);
			}
		});
	}
	
	// Add team to game
	private void addTeamToGame(final String teamName) {
		// Parse query
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("objectId", gameId);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			// Get requested game
			@Override
			public void done(final ParseObject game, ParseException e) {
				Log.d(LOGTAG, "REQUESTED GAME : "+game.getString("name"));
				if(e!=null){
					Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
				// Get existing teams in the game
				game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> teams, ParseException e) {
						Log.d(LOGTAG, "TEAMS");
						if(teams.isEmpty()) {
							ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
							teamQuery.whereEqualTo("name", teamName);
							teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {

								@Override
								public void done(final ParseObject team, ParseException e) {
									team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
										@Override
										public void done(List<ParseObject> players, ParseException e) {
											addTeam(game, team, players);
										}
									});
								}
								
							});
							return;
						}
						final ArrayList<String> existingPlayersId = new ArrayList<String>();
						for(ParseObject team : teams) {
							Log.d(LOGTAG, team.getString("name"));
							// For each team get players Id
							team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
								@Override
								public void done(List<ParseObject> players, ParseException e) {
									Log.d(LOGTAG, "PLAYERS");
									for(ParseObject player : players) {
										Log.d(LOGTAG, player.getString("username"));
										existingPlayersId.add(player.getObjectId());
									}
									// Get selected team
									ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
									teamQuery.whereEqualTo("name", teamName);
									teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
										@Override
										public void done(final ParseObject selectedTeam, ParseException e) {
											if(e!=null){
												Toast.makeText(context, "Sorry, this team doesn't exist.", Toast.LENGTH_SHORT).show();
												return;
											}
											Log.d(LOGTAG, "REQUESTED TEAM : "+selectedTeam.get("name"));
											// Get players Id for requested team to add
											selectedTeam.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {

												@Override
												public void done(final List<ParseObject> players, ParseException e) {
													boolean alreadyInGame = false;
													String playerId = null;
													for(ParseObject player : players) {
														Log.d(LOGTAG, "PLAYERS : "+player.getString("username"));
														playerId = player.getObjectId();
														if(existingPlayersId.contains(playerId)){
															alreadyInGame = true;
														}
													}
													if(alreadyInGame) {
														AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
														alertDialog.setTitle(selectedTeam.getString("name"));
														alertDialog.setMessage("Sorry, you are not allowed to add this team. One or more players are already in this game.");
														alertDialog.setPositiveButton("OK", null);
														return;
													}
													addTeam(game, selectedTeam, players);
													autocompleteValue.setText("");
												}
											});
										}
									});
								}
							});
						}
					}
				});
			}
		});
	}
	
	private void addTeam(final ParseObject game, ParseObject team, final List<ParseObject> players) {
		game.getRelation("teams").add(team);
		game.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					for(ParseObject player : players) {
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("userId", player.getObjectId());
						params.put("gameId", game.getObjectId());
						ParseCloud.callFunctionInBackground("addGameToUser", params, new FunctionCallback<String>() {
							public void done(String result, ParseException e) {
								if (e != null) {
									Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
					gameTeamsParseAdapter.loadObjects();
				}
			}
		});
	}	
}
