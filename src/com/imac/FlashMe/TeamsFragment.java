package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.imac.FlashMe.R;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
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

public class TeamsFragment extends ListFragment {
	
	private static final String LOGTAG = "TeamsFragment";
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private static List<String> playersList = null;
	private int state; // 0:teams, 1:detail
	private String teamName;
	
	// Layout elements
	private TeamParseAdapter teamParseAdapter;
	private TeamPlayersParseAdapter teamPlayersParseAdapter;
	private ArrayAdapter<String> playersAdapter;
	private ImageButton backButton;
	private ImageButton refreshButton;
	private EditText inputValue;
	private AutoCompleteTextView autocompleteValue;
	private Button addButton;
	//private Button playButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_teams, container, false);
		final Context context = mainView.getContext();
		
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		playersList = new ArrayList<String>();
		
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		backButton = (ImageButton) mainView.findViewById(R.id.back_bt);
		refreshButton = (ImageButton) mainView.findViewById(R.id.refresh_bt);
		inputValue = (EditText) mainView.findViewById(R.id.enter_team);
		autocompleteValue = (AutoCompleteTextView) mainView.findViewById(R.id.autocomplete_player);
		ParseQuery<ParseUser> playersQuery = ParseUser.getQuery();
		playersQuery.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> players, ParseException e) {
				initAutoCompleteList(players);
				autocompleteValue.setThreshold(1);
			}
		});
		addButton = (Button) mainView.findViewById(R.id.create_team);
    	//playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
    	teamParseAdapter = new TeamParseAdapter(getActivity(), currentUser);
    	teamParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.INVISIBLE);
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
    	
    	refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state) {
				case 0:
					teamParseAdapter.loadObjects();
					break;
				case 1:
					teamPlayersParseAdapter.loadObjects();
					break;
				default:
					break;
				}
				
			}
		});
    	
    	// Create team/Add player
    	addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				switch(state) {
				case 0:
					final String s_inputValue = inputValue.getText().toString();
					if(s_inputValue.equals("")){
						// Invalid team name
						Toast.makeText(getActivity(), R.string.empty_team_name, Toast.LENGTH_LONG).show();
					}
					else {
						createTeam(s_inputValue);
					}
					break;
				case 1:
					final String s_autocompleteValue = autocompleteValue.getText().toString();
					if(s_autocompleteValue.equals("")){
						// Invalid team name
						Toast.makeText(getActivity(), R.string.empty_player_name, Toast.LENGTH_LONG).show();
					}
					else {
						addPlayerToTeam(s_autocompleteValue);
					}
					break;
				default:
					break;
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
			teamName = team.getString("name");
			teamPlayersParseAdapter = new TeamPlayersParseAdapter(getActivity(), currentUser, team);
			setDetailAdapter(teamPlayersParseAdapter);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(LOGTAG, "onAttach");
		super.onAttach(activity);
	}
	
	@Override
	public void onStart() {
		Log.d(LOGTAG, "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.d(LOGTAG, "onResume");
		super.onResume();
	}
	
	private void initAutoCompleteList(List<ParseUser> players) {
		for(ParseUser player: players) {
			playersList.add(player.getUsername());
		}
		String[] playersArray = new String[playersList.size()];
		playersAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, playersList.toArray(playersArray));
		autocompleteValue.setAdapter(playersAdapter);
	}
	public void setGeneralAdapter() {
		state = 0;
		teamName= "";
		addButton.setBackgroundResource(R.drawable.dark_button);
		addButton.setText("CREATE");
		addButton.setEnabled(true);
		inputValue.setVisibility(View.VISIBLE);
		addButton.setVisibility(View.VISIBLE);
		autocompleteValue.setVisibility(View.GONE);
		backButton.setVisibility(View.INVISIBLE);
		
		setListAdapter(teamParseAdapter);
	}
	
	public void setDetailAdapter(TeamPlayersParseAdapter teamPlayersParseAdapter) {
		state = 1;
		boolean enable = true;
		inputValue.setVisibility(View.GONE);
		autocompleteValue.setVisibility(View.VISIBLE);
		addButton.setText("ADD");
		
		if(teamPlayersParseAdapter.isCreator()){
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
					inputValue.setText("");
				}
				else {
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	// Add player to team
	private void addPlayerToTeam(final String playerName) {
		// Parse query
		ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
		teamQuery.whereEqualTo("name", teamName);
		// Get concerned team
		teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(final ParseObject team, ParseException e) {
				if(e!=null){
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
				// Parse Query
				ParseQuery<ParseUser> playerQuery = ParseUser.getQuery();
				playerQuery.whereEqualTo("username", playerName);
				// Get selected player
				playerQuery.getFirstInBackground(new GetCallback<ParseUser>() {
					@Override
					public void done(ParseUser player, ParseException e) {
						if(e!=null){
							Toast.makeText(getActivity(), "Sorry, this player doesn't exist.", Toast.LENGTH_SHORT).show();
							return;
						}
						team.getRelation("players").add(player);
						team.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									teamPlayersParseAdapter.loadObjects();
								}
							}
						});
						autocompleteValue.setText("");
					}
				});
			}
		});
	}
}