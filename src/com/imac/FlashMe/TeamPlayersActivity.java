package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TeamPlayersActivity extends ListActivity {
	
	private static final String LOGTAG = "TeamPlayersActivity";
	private Context context;
	private LayoutInflater inflater;
	
	// Data elements
	private static ParseUser currentUser = null;
	private String teamId;
	private static List<String> playersList = null;
	
	// Layout elements
	private TextView title;
	private AutoCompleteTextView autocompleteValue;
	private Button addButton;
	private TeamPlayersParseAdapter teamPlayersParseAdapter;
	private ArrayAdapter<String> playersAdapter;
	private static ProgressBar progress = null;
	private ImageButton refreshButton;
	private ListView teamPlayersList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		context = TeamPlayersActivity.this;
		inflater = LayoutInflater.from(context);
		Intent intent = getIntent();
		teamId = intent.getStringExtra("TEAM_ID");
		
		getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
		
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		currentUser.put("state", 1);
		currentUser.saveInBackground();
		
		playersList = new ArrayList<String>();
		
		autocompleteValue = (AutoCompleteTextView) this.findViewById(R.id.autocomplete_player);
		ParseQuery<ParseUser> playersQuery = ParseUser.getQuery();
		playersQuery.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> players, ParseException e) {
				initAutoCompleteList(players);
				autocompleteValue.setThreshold(1);
			}
		});
		title = (TextView) this.findViewById(R.id.activity_title);
		addButton = (Button) this.findViewById(R.id.add_player);
		progress = (ProgressBar) this.findViewById(R.id.progressBar);
		refreshButton = (ImageButton) this.findViewById(R.id.refresh_bt);
		teamPlayersList = (ListView) this.findViewById(android.R.id.list);
		
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final String s_autocompleteValue = autocompleteValue.getText().toString();
				if(s_autocompleteValue.equals("")){
					// Invalid team name
					Toast.makeText(context, R.string.empty_player_name, Toast.LENGTH_LONG).show();
				}
				else {
					addPlayerToTeam(s_autocompleteValue);
				}
			}
		});
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				teamPlayersParseAdapter.loadObjects();
			}
		});
		
		
		initParseAdapter();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		currentUser.put("state", 0);
		currentUser.saveInBackground();
	}

	@Override
	protected void onResume() {
		super.onResume();
		currentUser.put("state", 1);
		currentUser.saveInBackground();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("Zizanie", "TeamPlayersActivity : onListItemClick");
		super.onListItemClick(l, v, position, id);
		
		ParseUser user = ((ParseUser) l.getItemAtPosition(position));
		String userId = user.getObjectId();
		
		final Intent intent = new Intent(context, ProfileActivity.class);
		intent.putExtra("USER", userId);
		startActivity(intent);

	}
	
	private void initAutoCompleteList(List<ParseUser> players) {
		for(ParseUser player: players) {
			playersList.add(player.getUsername());
		}
		String[] playersArray = new String[playersList.size()];
		playersAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, playersList.toArray(playersArray));
		autocompleteValue.setAdapter(playersAdapter);
	}
	
	private void initParseAdapter() {
		ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
		teamQuery.whereEqualTo("objectId", teamId);
		teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject team, ParseException e) {
				if(e != null) {
					Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.d(LOGTAG, "Get Parse Team : "+e.getMessage());
					return;
				}
				teamPlayersParseAdapter = new TeamPlayersParseAdapter(context, currentUser, team);
				title.setText(team.getString("name"));
				boolean enable = true;
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
				
				teamPlayersParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
					@Override
					public void onLoaded(List<ParseObject> arg0, Exception arg1) {
						teamPlayersList.setVisibility(View.VISIBLE);
						progress.setVisibility(View.INVISIBLE);
					}
					@Override
					public void onLoading() {
						progress.setVisibility(View.VISIBLE);
						teamPlayersList.setVisibility(View.INVISIBLE);
					}
		    	});
				setListAdapter(teamPlayersParseAdapter);
			}
		});
	}
	
	// Add player to team
	private void addPlayerToTeam(final String playerName) {
		// Parse query
		ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
		teamQuery.whereEqualTo("objectId", teamId);
		// Get concerned team
		teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(final ParseObject team, ParseException e) {
				if(e!=null){
					Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.d(LOGTAG, "Get Parse Team : "+e.getMessage());
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
							Toast.makeText(context, "Sorry, this player doesn't exist.", Toast.LENGTH_SHORT).show();
							Log.d(LOGTAG, "Get Parse User to add : "+e.getMessage());
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
