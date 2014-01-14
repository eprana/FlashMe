package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TeamsFragment extends Fragment {
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ArrayList<Team> teams = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private static ELVTeamAdapter teamAdapter;
	private static ExpandableListView expandableList = null;
	private EditText teamName;
	private Button createTeam;
	private Button playButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.teams, container, false);
		Context context = mainView.getContext();
		
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		teams = new ArrayList<Team>();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
    	teamAdapter = new ELVTeamAdapter(context, teams);        	
    	expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
    	teamName = (EditText) mainView.findViewById(R.id.enter_team);
    	createTeam = (Button) mainView.findViewById(R.id.create_team);
    	playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
        LoadTeams lt = new LoadTeams(context);
    	lt.execute();
    	
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
		playButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(teamAdapter.getSelectedTeam() != null){
					Toast.makeText(getActivity(), "Selected team : "+teamAdapter.getSelectedTeam().getName(), Toast.LENGTH_LONG).show();
				} 
				else {
					// If no team has been selected
					Toast.makeText(getActivity(), "Ooops! You must select a team to play." , Toast.LENGTH_LONG).show();
				}
			}
		});
		
    	return mainView;
	}
	
	private static class LoadTeams extends AsyncTask<Void, Integer, Void> {

		private Context context;
		
		public LoadTeams(Context context){
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Get user's teams with Parse to create Java Teams
			loadTeams(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);
			expandableList.setAdapter(teamAdapter);
		}
		
	}
	
	private static void loadTeams(final Context context){
		// Parse query
		ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
		teamsQuery.whereEqualTo("players", currentUser);
		teamsQuery.include("createdBy");
		teamsQuery.findInBackground(new FindCallback<ParseObject>() {
			// Find current user's teams with Parse
		    public void done(List<ParseObject> teamsList, ParseException e) {
		    	if( e != null ) {
		    		Toast.makeText(context, "Error : " + e.toString(), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	createTeams(context, teamsList);
	        }
		});
	}

	private static void createTeams(final Context context, List<ParseObject> parseTeams) {
		for (ParseObject team : parseTeams) {
			// Create java Team
			ParseObject creator = new ParseObject("User");
			creator = team.getParseObject("createdBy");
			final Team newTeam = new Team(team.getString("name"), creator.getString("username"), context.getResources().getDrawable(R.drawable.default_team_picture_thumb));
			// Get players of the team with Parse
			team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> players, ParseException e) {
					if(e != null) {
						Toast.makeText(context, "Error : " + e.toString(), Toast.LENGTH_LONG).show();
						return;
					}
					addPlayersToTeam(context, newTeam, players );
				}
			});
			teams.add(newTeam);
		}
	}
	
	private static void addPlayersToTeam(Context context, Team team, List<ParseObject> players) {
		for (ParseObject player : players) {
			// Create small avatar
	    	ParseFile avatarFile = (ParseFile) player.get("avatar");
	    	try {
	    		byte[] avatarByteArray = avatarFile.getData();
				Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
				avatarBitmap = Bitmap.createScaledBitmap(avatarBitmap, 110, 110, false);
	
				// Add java Player
				team.addPlayer(new Player(((ParseUser) player).getUsername(), avatarBitmap));
			} catch (ParseException e1) {
				Toast.makeText(context, "Error : "+e1.getMessage(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			}
		}
	}
	
	// Create team button
	
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
					newTeam.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								createJavaTeam(newTeam);
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
	
	private void createJavaTeam(ParseObject parseTeam) {
		// Create Java Object
		final Team javaTeam = new Team(parseTeam.getString("name"), currentUser.getUsername(), getResources().getDrawable(R.drawable.default_team_picture_thumb));
		teams.add(javaTeam);
		// Add relation with current user
		ParseRelation<ParseObject> teamsRelation = parseTeam.getRelation("players");
		teamsRelation.add(currentUser);
		parseTeam.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
					return;
				}
				// Get player's avatar
				ParseFile avatarFile = currentUser.getParseFile("avatar");
				try {
					byte[] avatarByteArray = avatarFile.getData();
					Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
					avatarBitmap = Bitmap.createScaledBitmap(avatarBitmap, 110, 110, false);
					// Add player to the team
					javaTeam.addPlayer(new Player(currentUser.getUsername(), avatarBitmap));
				} catch (ParseException e1) {
					Toast.makeText(getActivity(), "Error : "+e1.getMessage(), Toast.LENGTH_LONG).show();
					e1.printStackTrace();
				}
				
				// Update view
				expandableList.setAdapter(teamAdapter);
			}
		});
	}
}