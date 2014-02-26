package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.imac.FlashMe.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GamesFragment extends ListFragment {

	private static final String LOGTAG = "GamesFragment";
	
	// Data elements
	private ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private static List<String> teamsList = null;
	private int state; // 0:games, 1:detail
	private String gameName;
	
	// Layout elements
	private GameParseAdapter gameParseAdapter;
	private GameTeamsParseAdapter gameTeamsParseAdapter;
	private ArrayAdapter<String> teamsAdapter;
	private ImageButton backButton;
	private ImageButton refreshButton;
	private EditText inputValue;
	private AutoCompleteTextView autocompleteValue;
	private Button addButton;
	private Button playButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_games, container, false);
		final Context context = mainView.getContext();
    	
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		teamsList = new ArrayList<String>();
		
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		backButton = (ImageButton) mainView.findViewById(R.id.back_bt);
		refreshButton = (ImageButton) mainView.findViewById(R.id.refresh_bt);
    	inputValue = (EditText) mainView.findViewById(R.id.enter_game);
    	autocompleteValue = (AutoCompleteTextView) mainView.findViewById(R.id.autocomplete_team);
    	
		ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
		teamsQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> teams, ParseException e) {
				initAutoCompleteList(teams);
				autocompleteValue.setThreshold(1);
			}
		});
		addButton = (Button) mainView.findViewById(R.id.create_game);
		playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
    	gameParseAdapter = new GameParseAdapter(context, currentUser);
    	gameParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {

			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}

    	});
    	setListAdapter(gameParseAdapter);
    	
    	backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setGeneralAdapter();
			}
		});
    	
    	refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state) {
				case 0:
					gameParseAdapter.loadObjects();
					break;
				case 1:
					gameTeamsParseAdapter.loadObjects();
					break;
				default:
					break;
				}
				
			}
		});

    	// Create game button listener
    	addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state) {
				case 0:
					// Create new game
					final String s_inputValue = inputValue.getText().toString();
					if(s_inputValue.equals("")){
						// Empty edit text
						Toast.makeText(context, R.string.empty_game_name, Toast.LENGTH_LONG).show();
						return;
					}
					else {
						createGame(s_inputValue);
					}
					break;
				case 1:
					// Add team to game
					final String s_autocompleteValue = autocompleteValue.getText().toString();
					if(s_autocompleteValue.equals("")){
						// Empty edit text
						Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_LONG).show();
						return;
					}
					else {
						addTeamToGame(s_autocompleteValue);
					}
					break;
				default:
					break;
				}
			}
		});

		// Play button
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start game
				Log.d("Zizanie", "DEBUG : OnClickOnPlayButton");
				if(!gameName.isEmpty()) {
					final Intent intent = new Intent(getActivity(), GameActivity.class);
					intent.putExtra("GAME", gameName);			
					startActivity(intent);
				}
			}
		});

    	return mainView;	
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(LOGTAG, "onListItemClick");
		super.onListItemClick(l, v, position, id);
		if(state == 0) {
			ParseObject game = ((ParseObject) l.getItemAtPosition(position));
			gameName = game.getString("name");
			gameTeamsParseAdapter = new GameTeamsParseAdapter(getActivity(), currentUser, game);
			setDetailAdapter(gameTeamsParseAdapter);	
		}
	}
	
	private void initAutoCompleteList(List<ParseObject> teams) {
		for(ParseObject team: teams) {
			teamsList.add(team.getString("name"));
		}
		String[] teamsArray = new String[teamsList.size()];
		if(teamsArray != null) {
			teamsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, teamsList.toArray(teamsArray));
			autocompleteValue.setAdapter(teamsAdapter);
		}
	}
	
	public void setGeneralAdapter() {
		state = 0;
		gameName= "";
		addButton.setBackgroundResource(R.drawable.dark_button);
		addButton.setText("CREATE");
		addButton.setEnabled(true);
		inputValue.setVisibility(View.VISIBLE);
		addButton.setVisibility(View.VISIBLE);
		autocompleteValue.setVisibility(View.GONE);
		backButton.setVisibility(View.INVISIBLE);
		playButton.setVisibility(View.INVISIBLE);
		
		setListAdapter(gameParseAdapter);
	}
	
	public void setDetailAdapter(GameTeamsParseAdapter gameTeamsParseAdapter) {
		state = 1;
		boolean enable = true;
		inputValue.setVisibility(View.GONE);
		autocompleteValue.setVisibility(View.VISIBLE);
		addButton.setText("ADD");
		
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
		
		backButton.setVisibility(View.VISIBLE);
		playButton.setVisibility(View.VISIBLE);
		
		gameTeamsParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.GONE);
			}
			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}
    	});
		setListAdapter(gameTeamsParseAdapter);
	}
	
	// Create game
	private void createGame(final String name) {
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("name", name);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			// Check if a game already exists with the same name
			public void done(ParseObject arg0, ParseException e) {
				if(e==null){
					Toast.makeText(getActivity(), "Sorry, this name has already be taken.", Toast.LENGTH_LONG).show();
				}else if(e.getCode() == 101){
					// Create Parse game
					final ParseObject newGame = new ParseObject("Game");
					newGame.put("name", name);
					newGame.put("state", 0);
					newGame.put("createdBy", currentUser);
					newGame.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e != null) {
								Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
								return;
							}
							currentUser.getRelation("games").add(newGame);
							currentUser.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException e) {
									if (e == null) {
										gameParseAdapter.loadObjects();
									}
								}
							});
						}
					});
					// Clear edit text value
					inputValue.setText("");
				} else{
					Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	// Add team to game
	private void addTeamToGame(final String teamName) {
		// Parse query
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("name", gameName);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			// Get requested game
			@Override
			public void done(final ParseObject game, ParseException e) {
				Log.d(LOGTAG, "REQUESTED GAME : "+game.getString("name"));
				if(e!=null){
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
												Toast.makeText(getActivity(), "Sorry, this team doesn't exist.", Toast.LENGTH_SHORT).show();
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
														Toast.makeText(getActivity(), "Sorry, you are not allowed to add this team. One or more players are already in this game.", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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