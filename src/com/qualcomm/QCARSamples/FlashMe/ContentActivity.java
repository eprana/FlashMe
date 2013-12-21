package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class ContentActivity extends FragmentActivity {

  // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
	// Data to get
	final static String EXTRA_LOGIN = "user_login";
	private static ArrayList<Game> games = null;
	private static ArrayList<Team> teams = null;
  // ------------------------------------------------------------------------------------
	
	public static class ProfileFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.profile, container, false);	
			
		//	-------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
        	//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
	        //}
	    // --------------------------------------------------------------------------------------
			return mainView;
		}
	}
	
	public static class TeamsFragment extends Fragment {

		private ExpandableListView expandableList = null;
		private View alertDialogView;
	
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.teams, container, false);
			final Context context = mainView.getContext();
			
			//	-------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
        	//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
	        //}
	    // --------------------------------------------------------------------------------------
			expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
			teams = new ArrayList<Team>();

			//Teams tests
			Team team1 = createTeam("Anti-Heroes", "Zizi", getResources().getDrawable(R.drawable.team_empty_mini));
			team1.addPlayer(team1, "Zizi", getResources().getDrawable(R.drawable.pic_empty_mini));
			team1.addPlayer(team1, "Flo", getResources().getDrawable(R.drawable.pic_flo));
			team1.addPlayer(team1, "Xopi", getResources().getDrawable(R.drawable.pic_empty_mini));
			team1.setReady(true);
			
			Team team2 = createTeam("Hydro-G�ne", "Flo", getResources().getDrawable(R.drawable.team_empty_mini));
			team2.addPlayer(team2, "Flo", getResources().getDrawable(R.drawable.pic_flo));
			team2.addPlayer(team2, "Jiji", getResources().getDrawable(R.drawable.pic_empty_mini));
			team2.addPlayer(team2, "C�dric", getResources().getDrawable(R.drawable.pic_empty_mini));
			team2.setReady(true);

			//Players are ready? tests
			team1.getPlayers().get(0).setReady(true);
			team1.getPlayers().get(2).setReady(true);
			team2.getPlayers().get(0).setReady(true);
			team2.getPlayers().get(1).setReady(true);
			team2.getPlayers().get(2).setReady(true);
			
			//Changing picture tests
			team1.getPlayers().get(2).setPicture(getResources().getDrawable(R.drawable.pic_xopi));
					
			//Add players to team
			teams.add(team1);
			teams.add(team2);
			
			ELVTeamAdapter adapter = new ELVTeamAdapter(context, teams);
			expandableList.setAdapter(adapter);
			
			registerForContextMenu(expandableList);
			
//			expandableList.setOnItemLongClickListener(new OnItemLongClickListener() {
	            
//				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
					
//	            	// Create an alert box
//					AlertDialog.Builder adb = new AlertDialog.Builder(context);
//					MessageAlert msg_a;
//					
//					if (alertDialogView == null) {
//						msg_a = new MessageAlert();
//						alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
//						msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
//						alertDialogView.setTag(msg_a);
//					} else {
//						msg_a = (MessageAlert) alertDialogView.getTag();				
//					}
//					
//					// Choosing the type of message alert
//					msg_a.msg.setText(context.getResources().getString(R.string.quit_team));
//					
//					// Filling the alert box
//					adb.setView(alertDialogView);
//					adb.setTitle("What do you want to do ?");
//					adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//			            public void onClick(DialogInterface dialog, int which) {
//			            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
//							adbParent.removeView(alertDialogView);
//			        } });
//					adb.setPositiveButton("QUIT TEAM", new DialogInterface.OnClickListener() {
//			            public void onClick(DialogInterface dialog, int which) {
//			            	Intent intent = new Intent(context, ContentActivity.class);
//			            	startActivity(intent);
//			        } });
//					
//					// Showing the alert box
//			        adb.create();
//					adb.show();
//	            	
//	                return true;
//	            }
//	        }); 

			return mainView;
		}
				
		public Team createTeam(String name, String creator, Drawable picture){
			return new Team(name, creator, picture);
		}
						
	}

	public static class GamesFragment extends Fragment {
		
		private ExpandableListView expandableList = null;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.games, container, false);
			Context context = mainView.getContext();
			
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
			
			ELVGameAdapter adapter = new ELVGameAdapter(context, games);
			expandableList.setAdapter(adapter);
			
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
	 	
	 	final ImageButton profile_bt = (ImageButton) findViewById(R.id.profile_bt);
	 	final ImageButton teams_bt = (ImageButton) findViewById(R.id.team_bt);
		final ImageButton games_bt = (ImageButton) findViewById(R.id.game_bt);
	 	
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

		//if it is the first time created
		if(savedInstanceState == null){
			FragmentTransaction fragmentTransaction = myFragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.maincontainer, profileFrag, TAG_PROFILE);
			fragmentTransaction.commit();
		}
 	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(v.getId() == R.id.teams_list){
			super.onCreateContextMenu(menu, v, menuInfo);
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.context_menu_team, menu);
		}
		else if(v.getId() == R.id.games_list){
			super.onCreateContextMenu(menu, v, menuInfo);
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.context_menu_game, menu);
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.quit_team:
	        	//teams.removeSelectedTeam();
	            return true;
	        case R.id.cancel:
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}

