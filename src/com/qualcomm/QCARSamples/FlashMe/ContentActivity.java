package com.qualcomm.QCARSamples.FlashMe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.os.Bundle;

public class ContentActivity extends FragmentActivity {

	public static class ProfileFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
			View mainView = inflater.inflate(R.layout.profile, container, false);		
			return mainView;
		}
	}
	
	public static class TeamsFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
			View mainView = inflater.inflate(R.layout.teams, container, false);		
			return mainView;
		}
	}

	public static class GamesFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
			View mainView = inflater.inflate(R.layout.games, container, false);		
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
	 	
	 	ImageButton profile_bt = (ImageButton) findViewById(R.id.profile_bt);
	 	ImageButton teams_bt = (ImageButton) findViewById(R.id.team_bt);
		ImageButton games_bt = (ImageButton) findViewById(R.id.game_bt);
	 	
		profile_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
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
