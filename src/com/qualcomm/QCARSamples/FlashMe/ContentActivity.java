package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.parse.ParseException;
import com.parse.FindCallback;
import com.parse.ParseObject;
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
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
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
	
	public static class ProfileFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.profile, container, false);	
			//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
	        //}
			return mainView;
		}
	}
	
	public static class TeamsFragment extends Fragment {

		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.teams, container, false);
			final Context context = mainView.getContext();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
        	userName.setText(EXTRA_LOGIN);
			
			// Get list of teams
        	teams = new ArrayList<Team>();
			ParseRelation<ParseObject> teamsRelation = currentUser.getRelation("teams");
			teamsRelation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			    public void done(List<ParseObject> results, ParseException e) {
			        if (e == null) {
			        	for (ParseObject result : results) {
			        		 Team newTeam = new Team(result.getString("name"), currentUser.getUsername(), getResources().getDrawable(R.drawable.team_empty_mini));
			        		 newTeam.addPlayer(new Player(currentUser.getUsername(), getResources().getDrawable(R.drawable.pic_empty_mini)));
			        		 teams.add(newTeam);
			        	}
			        }
			    }
			});
			
			// Display teams
			teamAdapter = new ELVTeamAdapter(context, teams);
        	ContentActivity.expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
			expandableList.setAdapter(teamAdapter);

			// TO CHANGE -- Players are ready? tests
			//team1.getPlayers().get(0).setReady(true);
			//team1.getPlayers().get(2).setReady(true);
			
			// TO CHANGE -- Changing picture tests
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
						final ParseObject team = new ParseObject("Team");
						team.put("name", s_teamName);
						team.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									// Add relation with current user
									ParseRelation<ParseObject> teamsRelation = currentUser.getRelation("teams");
									teamsRelation.add(team);
									currentUser.saveInBackground();
									teams.add(new Team(team.getString("name"), currentUser.getUsername(), getResources().getDrawable(R.drawable.team_empty_mini)));
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
			
			//	-------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
        	//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
	        //}
        	// --------------------------------------------------------------------------------------
        	
        	expandableList = (ExpandableListView) mainView.findViewById(R.id.games_list);
			games = new ArrayList<Game>();

			//Games tests
			Game game1 = createGame("Remember, remember", "Zizi");
			game1.addTeam("Anti-Heroes", "Zizi", getResources().getDrawable(R.drawable.pic_team1));
			game1.addTeam("Heroes", "Fifoune", getResources().getDrawable(R.drawable.pic_team2));
			game1.setReady(true);
			
			Game game2 = createGame("Blabla", "Xopi");
			game2.addTeam("Ladyfense", "Xopi", getResources().getDrawable(R.drawable.pic_team1));
			game2.addTeam("Hydro-G�ne", "Flo", getResources().getDrawable(R.drawable.team_empty_mini));
			game2.setReady(true);

			//Teams are ready? tests
			game1.getTeams().get(0).setReady(true);
			game2.getTeams().get(0).setReady(true);
			game2.getTeams().get(1).setReady(true);
						
			//Add teams to the game
			games.add(game1);
			games.add(game2);
			
			gameAdapter = new ELVGameAdapter(context, games);
			expandableList.setAdapter(gameAdapter);

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
		
		public Game createGame(String name, String creator){
			return new Game(name, creator);
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
				profile_bt.setImageResource(R.drawable.profile_bt);
				teams_bt.setImageResource(R.drawable.team_bt_in);
				games_bt.setImageResource(R.drawable.game_bt_in);
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
				profile_bt.setImageResource(R.drawable.profile_bt_in);
				teams_bt.setImageResource(R.drawable.team_bt);
				games_bt.setImageResource(R.drawable.game_bt_in);
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
				profile_bt.setImageResource(R.drawable.profile_bt_in);
				teams_bt.setImageResource(R.drawable.team_bt_in);
				games_bt.setImageResource(R.drawable.game_bt);
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
