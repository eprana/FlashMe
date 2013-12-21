package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ContentActivity extends FragmentActivity {

  // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
	// Data to get
	final static String EXTRA_LOGIN = "user_login";
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
	
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.teams, container, false);
			Context context = mainView.getContext();
			
			//	-------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
        	//Intent intent = getIntent();
        	TextView userName = (TextView) mainView.findViewById(R.id.name);
	        //if(intent != null){
	        	//userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	userName.setText(EXTRA_LOGIN);
	        //}
	    // --------------------------------------------------------------------------------------
			expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
			ArrayList<Team> teams = new ArrayList<Team>();

			//Teams tests
			Team team1 = createTeam("Anti-Heroes", "Zizi");
			team1.addPlayer(team1, "Zizi", getResources().getDrawable(R.drawable.pic_empty_mini));
			team1.addPlayer(team1, "Flo", getResources().getDrawable(R.drawable.pic_flo));
			team1.addPlayer(team1, "Xopi", getResources().getDrawable(R.drawable.pic_empty_mini));
			team1.setReady(true);
			
			Team team2 = createTeam("Hydro-Gène", "Flo");
			team2.addPlayer(team2, "Flo", getResources().getDrawable(R.drawable.pic_flo));
			team2.addPlayer(team2, "Jiji", getResources().getDrawable(R.drawable.pic_empty_mini));
			team2.addPlayer(team2, "Cédric", getResources().getDrawable(R.drawable.pic_empty_mini));
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
			
			ELVAdapter adapter = new ELVAdapter(context, teams);
			expandableList.setAdapter(adapter);

			return mainView;
		}
				
		public Team createTeam(String name, String creator){
			return new Team(name, creator);
		}
	}

	public static class GamesFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View mainView = inflater.inflate(R.layout.games, container, false);		
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
}

//OLD PROFILE ACTIVITY TO RE-USE
//package com.qualcomm.QCARSamples.FlashMe;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.TextView;
//
//public class ProfileActivity extends Activity {
//
//    // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
//	// Data to get
//	final String EXTRA_LOGIN = "user_login";
//	final String EXTRA_PASSWORD = "user_password";
//    // ------------------------------------------------------------------------------------
//	
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//	 	setContentView(R.layout.profile_fragment);        
//                
//        // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
//        	Intent intent = getIntent();
//        	
//        	TextView userName = (TextView) findViewById(R.id.name);
//        
//	        if(intent != null){
//	        	userName.setText(intent.getStringExtra(EXTRA_LOGIN));
//	        	//score_txt2.setText(intent.getStringExtra(EXTRA_PASSWORD));
//	        }
//	    // --------------------------------------------------------------------------------------
//    } 
//}
