package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class ContentActivity extends FragmentActivity {

	static ParseUser currentUser = ParseUser.getCurrentUser();
	final static String EXTRA_LOGIN = currentUser.getUsername();
	private static ArrayList<Game> games = null;
	private static ArrayList<Team> teams = null;
	private static ELVTeamAdapter teamAdapter;
	private static ELVGameAdapter gameAdapter;
	private static ExpandableListView expandableList = null;
	private static ImageView avatarView = null;

	public static class ProfileFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.profile, container, false);	
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
        	avatarView = (ImageView) mainView.findViewById(R.id.profile_picture);
        	
        	// Setting username on top of the page
        	userName.setText(EXTRA_LOGIN);
        	
        	// Setting profile picture
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        	query.getInBackground(currentUser.getObjectId(), new GetCallback<ParseObject>() {
          	  public void done(ParseObject object, ParseException e) {
          	    if (e == null) {
          	    	// Getting the avatar form the database
          	    	ParseUser userd = (ParseUser) object;
          	    	ParseFile avatarFile = (ParseFile) userd.get("avatar");
          	    	try {
						byte[] avatarByteArray = avatarFile.getData();
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
						// Setting the imageView
						avatarView.setImageBitmap(avatarBitmap);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
          	    	
          	    } else{
          	    	Toast.makeText(getActivity(), "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
          	    }
          	  }
          	});
        	
			return mainView;
		}
	}
	
	public static class TeamsFragment extends Fragment {

		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.teams, container, false);
			final Context context = mainView.getContext();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
        	userName.setText(EXTRA_LOGIN);
			
			// Display teams
        	teams = new ArrayList<Team>();
        	teamAdapter = new ELVTeamAdapter(context, teams);        	
        	ContentActivity.expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
        	
        	// TODO : Use ParseQueryAdapter
        	/*ParseQueryAdapter<ParseObject> adapter =
    			new ParseQueryAdapter<ParseObject>(getActivity(), new ParseQueryAdapter.QueryFactory<ParseObject>() {
    			    public ParseQuery<ParseO
    			    bject> create() {
    			    	ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
        				teamsQuery.whereEqualTo("players", currentUser);
        				return teamsQuery;
    			    }
    			});
        	adapter.setTextKey("name");
        	ListView listView = (ListView) mainView.findViewById(R.id.teams_list);
        	listView.setAdapter(adapter);*/
        	
        	/* DOC EXAMPLE
			ParseQueryAdapter.QueryFactory<ParseObject> factory =
			new ParseQueryAdapter.QueryFactory<ParseObject>() {
				public ParseQuery create() {
					ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
        			teamsQuery.whereEqualTo("players", currentUser);
        			return teamsQuery;
				}
			};
 
			// Pass the factory into the ParseQueryAdapter's constructor.
			ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(this, factory);
			adapter.setTextKey("name");
 
			// Perhaps set a callback to be fired upon successful loading of a new set of ParseObjects.
			adapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
				public void onLoading() {
					// Trigger any "loading" UI
				}
	 
				public void onLoaded(List<ParseObject> objects, ParseException e) {
					// Execute any post-loading logic, hide "loading" UI
				}
			});

			// Attach it to your ListView, as in the example above
			ListView listView = (ListView) mainView.findViewById(R.id.teams_list);
			listView.setAdapter(adapter);*/

        	// Get teams where user is a player with Parse
        	ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
        	teamsQuery.whereEqualTo("players", currentUser);
        	teamsQuery.findInBackground(new FindCallback<ParseObject>() {
				// Parse query
			    public void done(List<ParseObject> results, ParseException e) {
			        if (e == null) {
			        	for (ParseObject result : results) {
			        		// Create java Team
			        		final Team newTeam = new Team(result.getString("name"), currentUser.getUsername(), getResources().getDrawable(R.drawable.default_team_picture_thumb));
			        		// Get players in team with Parse
			        		result.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
			        			public void done(List<ParseObject> players, ParseException e) {
			        				if (e == null) {
			        					for (ParseObject player : players) {
			        						// Add java Players
			        						newTeam.addPlayer(new Player(((ParseUser) player).getUsername(), getResources().getDrawable(R.drawable.default_profile_picture_thumb)));	
			        					}
			        				}
			        			}
			        		});
			        		teams.add(newTeam);
			        	}
		        		 // Update adapter
		        		 expandableList.setAdapter(teamAdapter);
			        }
			    }
			});

			// TESTS
			//team1.getPlayers().get(0).setReady(true);
			//team1.getPlayers().get(2).setPicture(getResources().getDrawable(R.drawable.pic_xopi));
			
			// Create team button
			final EditText teamName = (EditText) mainView.findViewById(R.id.enter_team);
			Button createButton = (Button) mainView.findViewById(R.id.create_team);
			createButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final String s_teamName = teamName.getText().toString();
					if(s_teamName.equals("")){
						// Invalid team name
						Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_SHORT).show();
						return;
					}
					else {
						// Create team
						final ParseObject newTeam = new ParseObject("Team");
						newTeam.put("name", s_teamName);
						newTeam.put("createdBy", currentUser);
						newTeam.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									// Create Java Object
									final Team javaTeam = new Team(newTeam.getString("name"), EXTRA_LOGIN, getResources().getDrawable(R.drawable.default_team_picture_thumb));
									teams.add(javaTeam);
									// Add relation with current user
									ParseRelation<ParseObject> teamsRelation = newTeam.getRelation("players");
									teamsRelation.add(currentUser);
									newTeam.saveInBackground(new SaveCallback() {
										@Override
										public void done(ParseException e) {
											if (e == null) {
												// Add javaPlayer
												javaTeam.addPlayer(new Player(EXTRA_LOGIN, getResources().getDrawable(R.drawable.default_profile_picture_thumb)));
												// Update view
												expandableList.setAdapter(teamAdapter);
											}
										}
									});
								}
							}
						});
						teamName.setText("");
					}
				}
			});
			
			// Play button
			Button playButton = (Button) mainView.findViewById(R.id.play);
			playButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(teamAdapter.getSelectedTeam() != null){
						Toast.makeText(context, "selected team : "+ teamAdapter.getSelectedTeam().getName(), Toast.LENGTH_SHORT).show();
					} 
					// If no team has been selected
					else {
						Toast.makeText(context, "Ooops! You must select a team to play." , Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			// Create a contextual menu to delete teams
			registerForContextMenu(expandableList);
			
			return mainView;
		}						
	}

	public static class GamesFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.games, container, false);
			final Context context = mainView.getContext();
        	//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
        	
        	// Display games
        	games = new ArrayList<Game>();
        	gameAdapter = new ELVGameAdapter(context, games);        	
        	ContentActivity.expandableList = (ExpandableListView) mainView.findViewById(R.id.games_list);

        	// Get current user's existing games with Parse
        	ParseQuery<ParseObject> gamesQuery = ParseQuery.getQuery("Game");
        	gamesQuery.findInBackground(new FindCallback<ParseObject>() {
        	    public void done(List<ParseObject> gameList, ParseException e) {
        	        if (e == null) {
        	        	for (final ParseObject game : gameList) {
        	        		// Create java Game
							try {
								final Game newGame = new Game(game.getString("name"), ((ParseUser) game.fetch().getParseObject("createdBy")).fetch().getUsername());
							
								// Get teams in game with Parse
				        		game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
				        			public void done(List<ParseObject> teamsList, ParseException e) {
				        				if (e == null) {
				        					for (ParseObject team : teamsList) {
				        						// Add java Teams
				        						//((ParseUser) team.fetch().getParseObject("createdBy")).fetch().getUsername()
												newGame.addTeam(new Team(team.getString("name"), "creator", getResources().getDrawable(R.drawable.default_team_picture_thumb)));
				        					}
				        				}
			        				}
			        			});
				        		games.add(newGame);
				        		
							} catch (NotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
			        	}
        	        	// Update adapter
        	        	expandableList.setAdapter(gameAdapter);
        	        }
        	    }
        	});
			
			//TESTS
			//game1.getTeams().get(0).setReady(true);

			// Create game button
			final EditText gameName = (EditText) mainView.findViewById(R.id.enter_game);
			Button createButton = (Button) mainView.findViewById(R.id.create_game);
			createButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final String s_gameName = gameName.getText().toString();
					if(s_gameName.equals("")){
						// Invalid game name
						Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_SHORT).show();
						return;
					}
					else {
						// Create game
						final ParseObject newGame = new ParseObject("Game");
						newGame.put("name", s_gameName);
						newGame.put("createdBy", currentUser);
						newGame.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									games.add(new Game(newGame.getString("name"), EXTRA_LOGIN));
									expandableList.setAdapter(gameAdapter);
								}
							}
						});
						gameName.setText("");
					}
				}
			});

			// Play button
			Button playButton = (Button) mainView.findViewById(R.id.play);
			playButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(gameAdapter.getSelectedGame() != null){
						Toast.makeText(context, "selected team : "+ gameAdapter.getSelectedGame().getName(), Toast.LENGTH_SHORT).show();
					} 
					// If no team has been selected
					else {
						Toast.makeText(context, "Ooops! You must select a game to play." , Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			registerForContextMenu(expandableList);
        	
			return mainView;
		}
	}
	
	private FragmentManager myFragmentManager;
	private ProfileFragment profileFrag;
	private TeamsFragment teamsFrag;
	private GamesFragment gamesFrag;
	final static String TAG_PROFILE = "PROFILE_FRAGMENT";
	final static String TAG_TEAMS = "TEAMS_FRAGMENT";
	final static String TAG_GAMES = "GAMES_FRAGMENT";
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.content);
	 	
	 	// Top menu
	 	final ImageButton profile_bt = (ImageButton) findViewById(R.id.profile_bt);
	 	final ImageButton teams_bt = (ImageButton) findViewById(R.id.team_bt);
		final ImageButton games_bt = (ImageButton) findViewById(R.id.game_bt);
	 	
		// On profile icon click
		profile_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				profile_bt.setImageResource(R.drawable.menu_profile_bt);
				teams_bt.setImageResource(R.drawable.menu_teams_bt_in);
				games_bt.setImageResource(R.drawable.menu_games_bt_in);
				ProfileFragment fragment = (ProfileFragment)myFragmentManager.findFragmentByTag(TAG_PROFILE);
				if(fragment==null){
					FragmentTransaction fragmentTransaction = myFragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.maincontainer, profileFrag, TAG_PROFILE);
					fragmentTransaction.commit();
				}
			}
		});
		
		// On teams icon click
		teams_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				profile_bt.setImageResource(R.drawable.menu_profile_bt_in);
				teams_bt.setImageResource(R.drawable.menu_teams_bt);
				games_bt.setImageResource(R.drawable.menu_games_bt_in);
				TeamsFragment fragment = (TeamsFragment)myFragmentManager.findFragmentByTag(TAG_TEAMS);
				if(fragment==null){
					FragmentTransaction fragmentTransaction = myFragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.maincontainer, teamsFrag, TAG_TEAMS);
					fragmentTransaction.commit();
				}
			}
		});
		
		// On games icon click
		games_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				profile_bt.setImageResource(R.drawable.menu_profile_bt_in);
				teams_bt.setImageResource(R.drawable.menu_teams_bt_in);
				games_bt.setImageResource(R.drawable.menu_games_bt);
				GamesFragment fragment = (GamesFragment)myFragmentManager.findFragmentByTag(TAG_GAMES);
				if(fragment==null){
					FragmentTransaction fragmentTransaction = myFragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.maincontainer, gamesFrag, TAG_GAMES);
					fragmentTransaction.commit();
				}
			}
		});
				
		myFragmentManager = getSupportFragmentManager();
		profileFrag = new ProfileFragment();
		teamsFrag = new TeamsFragment();
		gamesFrag = new GamesFragment();

		// If it is created for the first time
		if(savedInstanceState == null){
			FragmentTransaction fragmentTransaction = myFragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.maincontainer, profileFrag, TAG_PROFILE);
			fragmentTransaction.commit();
		}
 	}
	
	// Contextual menu for quitting teams / games
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if(type == 0){
			if(v.getId() == R.id.teams_list){
			    MenuInflater inflater = getMenuInflater();
			    inflater.inflate(R.menu.context_menu_team, menu);
			}
			else if(v.getId() == R.id.games_list){
			    MenuInflater inflater = getMenuInflater();
			    inflater.inflate(R.menu.context_menu_game, menu);
		    }
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		
	    switch (item.getItemId()) {
	        case R.id.quit_team:
	        	if(teams.remove(info.packedPosition)) Log.v("ca marche ", " ");

	            teamAdapter.notifyDataSetChanged();
	            return true;
	        case R.id.quit_game:

	        	return true;
	        case R.id.cancel:
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	public Team getTeamByName(String name) {
		for(Iterator<Team> it = teams.iterator(); it.hasNext();) {
			Team team = it.next();
			if(team.getName() == name) {
				return team;
			}
		}
		return null;
	}
	
	public Game getGameByName(String name) {
		for(Iterator<Game> it = games.iterator(); it.hasNext();) {
			Game game = it.next();
			if(game.getName() == name) {
				return game;
			}
		}
		return null;
	}
}

