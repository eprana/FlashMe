package com.qualcomm.QCARSamples.FlashMe;

import java.util.List;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TeamPlayersFragment extends Fragment {

	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private TeamPlayersParseAdapter teamPlayersParseAdapter;
	private ListView playersList;
	private EditText playerName;
	private Button addPlayer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.teams, container, false);
		Context context = mainView.getContext();
		
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
    	playerName = (EditText) mainView.findViewById(R.id.enter_team);
    	addPlayer = (Button) mainView.findViewById(R.id.create_team);
    	//playButton = (Button) mainView.findViewById(R.id.play);
    	
    	
    	// Load fragment data
    	playersList = (ListView) mainView.findViewById(R.id.teams_list);
    	teamPlayersParseAdapter = new TeamPlayersParseAdapter(context, currentUser);
    	playersList.setAdapter(teamPlayersParseAdapter);
    	teamPlayersParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {

			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.GONE);
			}

			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}

    	});
    	
    	// Create team button listener
		addPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_playerName = playerName.getText().toString();
				if(s_playerName.equals("")){
					// Invalid team name
					Toast.makeText(getActivity(), R.string.empty_player_name, Toast.LENGTH_LONG).show();
					return;
				}
				else {
					//addPlayer(s_playerName);
				}
			}
		});
		
    	return mainView;
	}
}
