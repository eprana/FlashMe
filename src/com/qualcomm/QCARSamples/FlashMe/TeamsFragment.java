package com.qualcomm.QCARSamples.FlashMe;

import java.util.List;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TeamsFragment extends ListFragment {
	
	public static String TAG="TAG_TEAMS";
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private int state; // 0:team, 1:detail
	
	// Layout elements
	private TeamParseAdapter teamParseAdapter;
	private TeamPlayersParseAdapter teamPlayersParseAdapter;
	private ImageButton backButton;
	private EditText teamName;
	private Button createTeam;
	//private Button playButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.teams, container, false);
		Context context = mainView.getContext();
		
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
		backButton = (ImageButton) mainView.findViewById(R.id.back_bt);
    	teamName = (EditText) mainView.findViewById(R.id.enter_team);
    	createTeam = (Button) mainView.findViewById(R.id.create_team);
    	//playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
    	teamParseAdapter = new TeamParseAdapter(getActivity(), currentUser);
    	teamParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.GONE);
			}
			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}
    	});
    	setListAdapter(teamParseAdapter);
    	
    	backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setGeneralAdapter();
			}
		});
    	
    	// Create team button listener
		createTeam.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_teamName = teamName.getText().toString();
				if(s_teamName.equals("")){
					// Invalid team name
					Toast.makeText(getActivity(), R.string.empty_team_name, Toast.LENGTH_LONG).show();
					return;
				}
				else {
					createTeam(s_teamName);
				}
			}
		});
		
		// Create play button listener
		/*playButton.setOnClickListener(new OnClickListener() {
			
			@Overrideb
			public void onClick(View v) {
				if(teamAdapter.getSelectedTeam() != null){
					Toast.makeText(getActivity(), "Selected team : "+teamAdapter.getSelectedTeam().getName(), Toast.LENGTH_LONG).show();
				} 
				else {
					// If no team has been selected
					Toast.makeText(getActivity(), "Ooops! You must select a team to play." , Toast.LENGTH_LONG).show();
				}
			}
		});*/
		
    	return mainView;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		
		ParseObject team = ((ParseObject) l.getItemAtPosition(position));
		teamPlayersParseAdapter = new TeamPlayersParseAdapter(getActivity(), currentUser, team);
		setDetailAdapter(teamPlayersParseAdapter);
	}
	
	public void setGeneralAdapter() {
		state = 0;
		backButton.setVisibility(View.INVISIBLE);
		
		setListAdapter(teamParseAdapter);
	}
	
	public void setDetailAdapter(TeamPlayersParseAdapter teamPlayersParseAdapter) {
		state = 1;
		backButton.setVisibility(View.VISIBLE);
		
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
		setListAdapter(teamPlayersParseAdapter);
	}
	
	// Create team
	private void createTeam(final String name) {
		// Parse query
		ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
		teamQuery.whereEqualTo("name", name);
		teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			// Check if a team already exists with the same name
			public void done(ParseObject arg0, ParseException e) {
				if(e==null){
					Toast.makeText(getActivity(), "Sorry, this name has already be taken.", Toast.LENGTH_SHORT).show();
					return;
				}
				if(e.getCode() == 101){
					// Create Parse Team
					final ParseObject newTeam = new ParseObject("Team");
					newTeam.put("name", name);
					newTeam.put("createdBy", currentUser);
					newTeam.getRelation("players").add(currentUser);
					newTeam.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								teamParseAdapter.loadObjects();
							}
						}
					});
					teamName.setText("");
				}
				else {
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}