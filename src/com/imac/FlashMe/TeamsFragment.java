package com.imac.FlashMe;

import java.util.List;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.SaveCallback;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TeamsFragment extends ListFragment {
	
	private static final String LOGTAG = "TeamsFragment";
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private int state; // 0:teams, 1:detail
	private String teamId;
	
	// Layout elements
	private TeamParseAdapter teamParseAdapter;
	private ImageButton refreshButton;
	private EditText inputValue;
	private Button createButton;
	private ListView teamsList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_teams, container, false);
		final Context context = mainView.getContext();
		
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		refreshButton = (ImageButton) mainView.findViewById(R.id.refresh_bt);
		inputValue = (EditText) mainView.findViewById(R.id.enter_team);
		createButton = (Button) mainView.findViewById(R.id.create_team);
		teamsList = (ListView) mainView.findViewById(android.R.id.list);
    	
    	// Load fragment data
    	teamParseAdapter = new TeamParseAdapter(getActivity(), currentUser);
    	teamParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				teamsList.setVisibility(View.VISIBLE);
				progress.setVisibility(View.INVISIBLE);
			}
			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
				teamsList.setVisibility(View.INVISIBLE);
			}
    	});
    	setListAdapter(teamParseAdapter);
    	
    	refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				teamParseAdapter.loadObjects();
			}
		});
    	
    	// Create team
    	createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final String s_inputValue = inputValue.getText().toString();
				if(s_inputValue.equals("")){
					// Invalid team name
					Toast.makeText(getActivity(), R.string.empty_team_name, Toast.LENGTH_LONG).show();
				}
				else {
					createTeam(s_inputValue);
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
			super.onListItemClick(l, v, position, id);
			ParseObject team = ((ParseObject) l.getItemAtPosition(position));
			teamId = team.getObjectId();
			// Start detail activity
			final Intent intent = new Intent(getActivity(), TeamPlayersActivity.class);
			intent.putExtra("TEAM_ID", teamId);
			startActivity(intent);
		}
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
					inputValue.setText("");
				}
				else {
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
					Log.d(LOGTAG, e.getMessage());
				}
			}
		});
	}
}