package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.widget.ExpandableListView.OnChildClickListener;

public class ContentActivity extends FragmentActivity{

	static ParseUser currentUser = ParseUser.getCurrentUser();
	final static String EXTRA_LOGIN = currentUser.getUsername();
	private static ArrayList<Game> games = null;
	private static ArrayList<Team> teams = null;
	private static ELVTeamAdapter teamAdapter;
	private static ELVGameAdapter gameAdapter;
	private static ExpandableListView expandableList = null;
	private static ImageView avatarView = null;
	private static int itemSelected = 0;
	private ExpandableListView sList;
	private SettingsAdapter sAdapter;
	
	// Fragments
	
	private String mFragment;
	private ProfileFragment mProfileFragment;
	private TeamsFragment mTeamsFragment;
	private GamesFragment mGamesFragment;
	private static String TAG_PROFILE = "PROFILE_FRAGMENT";
	private static String TAG_TEAMS = "TEAMS_FRAGMENT";
	private static String TAG_GAMES = "GAMES_FRAGMENT";
	
	private void setupFragments() {
		final FragmentManager fm = getSupportFragmentManager();
		
		mProfileFragment = (ProfileFragment) fm.findFragmentByTag(TAG_PROFILE);
		if (mProfileFragment == null) {
			mProfileFragment = new ProfileFragment();
		}
		mTeamsFragment = (TeamsFragment) fm.findFragmentByTag(TAG_TEAMS);
		if (mTeamsFragment == null) {
			mTeamsFragment = new TeamsFragment();
		}
		mGamesFragment = (GamesFragment) fm.findFragmentByTag(TAG_GAMES);
		if (mGamesFragment == null) {
			mGamesFragment = new GamesFragment();
		}		
	}

	private void showFragment(final Fragment fragment) {
		if (fragment == null){
			return;
		}
		final FragmentManager fm = getSupportFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		// Animate the changing of fragment
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		ft.replace(R.id.maincontainer, fragment);
		ft.commit();
	}
	
	// Menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_profile:
	        	showFragment(this.mProfileFragment);
	            return true;
	        case R.id.action_teams:
	        	showFragment(this.mTeamsFragment);
	            return true;
	        case R.id.action_games:
	        	showFragment(this.mGamesFragment);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private static class LoadProfile extends AsyncTask<Void, Integer, Void> {

		private Context context;
		
		public LoadProfile(Context ctxt){
			context = ctxt;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			Toast.makeText(context, "Début du traitement asynchrone", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... arg0) {

    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", currentUser.getUsername());
    	query.getFirstInBackground(new GetCallback<ParseUser>() {
			public void done(ParseUser user, ParseException e) {
			    if (e == null) {
			    	ParseFile avatarFile = (ParseFile) user.get("avatar");
			    	try {
						byte[] avatarByteArray = avatarFile.getData();
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
						// Setting the imageView
						avatarView.setImageBitmap(avatarBitmap);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
			    } else{
		  	    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		  	    }
			  }
			});
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			Toast.makeText(context, "Le traitement asynchrone est terminé", Toast.LENGTH_SHORT).show();
			//updateMenu();
		}
	}
	
	public static class ProfileFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.profile, container, false);
			final Context context = mainView.getContext();
        	avatarView = (ImageView) mainView.findViewById(R.id.profile_picture);

        	// Setting profile picture
        	LoadProfile lp = new LoadProfile(context);
        	lp.execute();

			return mainView;
		}
	}
	
	private static class LoadTeams extends AsyncTask<Void, Integer, Void> {

		private Context context;
		
		public LoadTeams(Context ctxt){
			context = ctxt;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			Toast.makeText(context, "Début du traitement asynchrone", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			// Get teams where user is a player with Parse
        	ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
        	teamsQuery.whereEqualTo("players", currentUser);
        	teamsQuery.findInBackground(new FindCallback<ParseObject>() {
				// Parse query
			    public void done(List<ParseObject> results, ParseException e) {
			        if (e == null) {
			        	for (ParseObject result : results) {
			        		// Create java Team
			        		final Team newTeam = new Team(result.getString("name"), currentUser.getUsername(), context.getResources().getDrawable(R.drawable.default_team_picture_thumb));
			        		// Get players in team with Parse
			        		result.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
			        			public void done(List<ParseObject> players, ParseException e) {
			        				if (e == null) {
			        					for (ParseObject player : players) {
			        						// Add java Players
			        						newTeam.addPlayer(new Player(((ParseUser) player).getUsername(),  context.getResources().getDrawable(R.drawable.default_profile_picture_thumb)));	
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
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			Toast.makeText(context, "Le traitement asynchrone est terminé", Toast.LENGTH_SHORT).show();
			//updateMenu();
		}
	}
	
	public static class TeamsFragment extends Fragment {
		
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.teams, container, false);
			final Context context = mainView.getContext();
			
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
        	LoadTeams lt = new LoadTeams(context);
        	lt.execute();
        	
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
						
						// Check if the name does not exist yet
						ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
						teamQuery.whereEqualTo("name", s_teamName);
						teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							@Override
							public void done(ParseObject arg0, ParseException e) {
								if(e==null){
									Toast.makeText(context, "Sorry, this name has already be taken.", Toast.LENGTH_SHORT).show();
								}else if(e.equals(101)){
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
								} else{
									Toast.makeText(context, "An error occured.", Toast.LENGTH_SHORT).show();
								}
							}
						});
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
			
			return mainView;
		}						
	}
	
	private static class LoadGames extends AsyncTask<Void, Integer, Void> {

		private Context context;
		
		public LoadGames(Context ctxt){
			context = ctxt;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Toast.makeText(getApplicationContext(), "Début du traitement asynchrone", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... arg0) {

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
												newGame.addTeam(new Team(team.getString("name"), "creator", context.getResources().getDrawable(R.drawable.default_team_picture_thumb)));
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
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			//Toast.makeText(getApplicationContext(), "Le traitement asynchrone est terminé", Toast.LENGTH_SHORT).show();
			//updateMenu();
		}
	}

	public static class GamesFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.games, container, false);
			final Context context = mainView.getContext();
        	
        	// Display games
        	games = new ArrayList<Game>();
        	gameAdapter = new ELVGameAdapter(context, games);        	
        	ContentActivity.expandableList = (ExpandableListView) mainView.findViewById(R.id.games_list);

        	// Get current user's existing games with Parse
        	LoadGames lg = new LoadGames(context);
        	lg.execute();
			
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
						// Check if the name does not exist yet
						ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
						gameQuery.whereEqualTo("name", s_gameName);
						gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							@Override
							public void done(ParseObject arg0, ParseException e) {
								if(e==null){
									Toast.makeText(context, "Sorry, this name has already be taken.", Toast.LENGTH_SHORT).show();
								}else if(e.getCode() == 101){
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
								} else{
									Toast.makeText(context, "An error occured.", Toast.LENGTH_SHORT).show();
								}
							}
						});
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
     	
			return mainView;
		}
	}
			
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.content);

	 	// Set fragment
	 	if (savedInstanceState != null)
	 		mFragment = savedInstanceState.getString("fragment");
	 	else
	 		mFragment = getIntent().getStringExtra("fragment");
	 	
	 	System.out.println("mFragment : "+mFragment);
	 	
	 	if(mFragment == null) {
	 		mFragment = "ProfileFragment";
	 	}
	 	
		setupFragments();
		if (mFragment.equals("ProfileFragment")) {
			showFragment(this.mProfileFragment);
		} else if (mFragment.equals("TeamsFragment")) {
			showFragment(this.mTeamsFragment);
		} else if (mFragment.equals("GamesFragment")) {
			showFragment(this.mGamesFragment);
		}
		
    	// Setting username on top of the page
    	TextView userName = (TextView) findViewById(R.id.name);
    	userName.setText(EXTRA_LOGIN);
		
    	// Settings button expandable list
    	sList = (ExpandableListView)findViewById(R.id.s_list);
    	sList.setDivider(null);
    	ArrayList<Settings> settings_bt = new ArrayList<Settings>();
		Settings setting = new Settings(getResources().getDrawable(R.drawable.settings_bt));
		ArrayList<SettingsButton> buttons = new ArrayList<SettingsButton>();
		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.edit_bt), "Edit profile"));
		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.logout_bt), "Log out"));
		setting.setSettingsButtons(buttons);
		settings_bt.add(setting);
		sAdapter = new SettingsAdapter(this, settings_bt);

		sList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if(groupPosition == 0 && childPosition == 1){
					//Log out
					ParseUser.logOut();
					finish();
				}
				else if(groupPosition == 0 && childPosition == 0){
					//Edit profile
					Intent intent = new Intent(getApplicationContext(), EditActivity.class);
	            	startActivity(intent);
				}
				
				return false;
			}
		});
		
		sList.setAdapter(sAdapter);
		
		// Top menu
	 	/*final ImageButton profile_bt = (ImageButton) findViewById(R.id.profile_bt);
	 	final ImageButton teams_bt = (ImageButton) findViewById(R.id.team_bt);
		final ImageButton games_bt = (ImageButton) findViewById(R.id.game_bt);*/
		final TextView top_line = (TextView) findViewById(R.id.top_line_light);
				
		/*profile_bt.setImageResource(R.drawable.menu_profile_bt);
		top_line.setText(R.string.my_profile);
		
		// On profile icon click
		profile_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				
				itemSelected = 0;
				profile_bt.setImageResource(R.drawable.menu_profile_bt);
				teams_bt.setImageResource(R.drawable.menu_teams_bt_in);
				games_bt.setImageResource(R.drawable.menu_games_bt_in);
				top_line.setText(R.string.my_profile);
				
				updateMenu();
			}
		});
		
		// On teams icon click
		teams_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){

				itemSelected = 1;
				profile_bt.setImageResource(R.drawable.menu_profile_bt_in);
				teams_bt.setImageResource(R.drawable.menu_teams_bt);
				games_bt.setImageResource(R.drawable.menu_games_bt_in);
				top_line.setText(R.string.my_teams);
				
				updateMenu();
			}
		});
		
		// On games icon click
		
		games_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){

				itemSelected = 2;
				profile_bt.setImageResource(R.drawable.menu_profile_bt_in);
				teams_bt.setImageResource(R.drawable.menu_teams_bt_in);
				games_bt.setImageResource(R.drawable.menu_games_bt);
				top_line.setText(R.string.my_games);
				
				updateMenu();
			}
		});*/
 	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment", mFragment != null ? mFragment : "");
		super.onSaveInstanceState(outState);
	}
	
	public class Settings {
		private Drawable settingsBt;
		private ArrayList<SettingsButton> sButtons;

		public Settings(Drawable bt) {
			super();
			this.settingsBt = bt;
			this.sButtons = new ArrayList<SettingsButton>();
		}

		//Getters & Setters
		public Drawable getDrawable() {	return settingsBt; }
		public ArrayList<SettingsButton> getSettingsButtons() { return sButtons; }
		public void setSettingsButtons(ArrayList<SettingsButton> buttons) { this.sButtons = buttons; }
	}
	
	
	public class SettingsButton {

		private Settings settingsBt;
		private Drawable picto;
		private String picto_tx;

		public SettingsButton(Settings bt, Drawable picto, String picto_tx) {
			super();
			this.settingsBt = bt;
			this.picto = picto;
			this.picto_tx = picto_tx;
		}

		//Getters & Setters
		public Drawable getDrawable(){ return this.picto; }
		public void setDrawable(Drawable picto){ this.picto = picto; }
		public String getTextPicto() { return picto_tx;	}
		public void setTextPicto(String picto_tx) { this.picto_tx = picto_tx; }

	}
	
	public class SettingsAdapter extends BaseExpandableListAdapter {

		private Context context;
		private ArrayList<Settings> settings;
		private LayoutInflater inflater;

		public SettingsAdapter(Context context, ArrayList<Settings> button) {
			this.context = context;
			this.settings = button;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public boolean areAllItemsEnabled() { return true; }
		public Object getChild(int gPosition, int cPosition) { return settings.get(gPosition).getSettingsButtons().get(cPosition); }
		public long getChildId(int gPosition, int cPosition) { return cPosition; }

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			final SettingsButton sButton = (SettingsButton) getChild(groupPosition, childPosition);
			BViewHolder bViewHolder;

	        if (convertView == null) {
	        	bViewHolder = new BViewHolder();
	            convertView = inflater.inflate(R.layout.settings_buttons, null);
	            bViewHolder.picto = (ImageView) convertView.findViewById(R.id.picto);
	            bViewHolder.picto_tx = (TextView) convertView.findViewById(R.id.picto_tx);
	            convertView.setTag(bViewHolder);
	        } else bViewHolder = (BViewHolder) convertView.getTag();

	        bViewHolder.picto.setImageDrawable(sButton.getDrawable());
	        bViewHolder.picto_tx.setText(sButton.getTextPicto());

	        return convertView;
		}

		public int getChildrenCount(int gPosition) { return settings.get(gPosition).getSettingsButtons().size(); }
		public Object getGroup(int gPosition) { return settings.get(gPosition); }
		public int getGroupCount() { return settings.size(); }
		public long getGroupId(int gPosition) { return gPosition; }

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			GViewHolder gholder;
			Settings settings_bt = (Settings) getGroup(groupPosition);

	        if (convertView == null) {
	        	gholder = new GViewHolder();
	        	convertView = inflater.inflate(R.layout.settings_row, null);
	        	gholder.settings_bt = (ImageView) convertView.findViewById(R.id.settings_bt);
	        	convertView.setTag(gholder);
	        } else gholder = (GViewHolder) convertView.getTag();

	        gholder.settings_bt.setImageDrawable(settings_bt.getDrawable());
	        return convertView;
		}

		public boolean hasStableIds() {	return true; }

		public boolean isChildSelectable(int arg0, int arg1) { 	return true; }
		
		class GViewHolder {
			public ImageView settings_bt;
		}

		class BViewHolder {
			public ImageView picto;
			public TextView picto_tx;
		}
	}
}

