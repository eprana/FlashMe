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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class TeamsFragment extends Fragment {
	
	private ParseUser currentUser = null;
	private ArrayList<Team> teams = null;
	
	private static ELVTeamAdapter teamAdapter;
	private static ExpandableListView expandableList = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.teams, container, false);
		Context context = mainView.getContext();
		
		// Get current user
		currentUser = ParseUser.getCurrentUser();
		
		// Initialize teams ArrayList
		teams = new ArrayList<Team>();
		
		// Display teams in expandable list
    	teamAdapter = new ELVTeamAdapter(context, teams);        	
    	expandableList = (ExpandableListView) mainView.findViewById(R.id.teams_list);
    	expandableList.setAdapter(teamAdapter);
    	
		// Get user's teams with Parse to create Java Teams
		this.loadTeams();
    	
    	// Create team button
		final EditText teamName = (EditText) mainView.findViewById(R.id.enter_team);
		Button createButton = (Button) mainView.findViewById(R.id.create_team);
		createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_teamName = teamName.getText().toString();
				if(s_teamName.equals("")){
					// Invalid team name
					Toast.makeText(getActivity(), R.string.empty_team_name, Toast.LENGTH_SHORT).show();
					return;
				}
				else {
					
					// Check if the name does not exist yet
					ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
					teamQuery.whereEqualTo("name", s_teamName);
					teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject arg0, ParseException e) {
							if(e==null){
								Toast.makeText(getActivity(), "Sorry, this name has already be taken.", Toast.LENGTH_SHORT).show();
							}else if(e.getCode() == 101){
								final ParseObject newTeam = new ParseObject("Team");
								newTeam.put("name", s_teamName);
								newTeam.put("createdBy", currentUser);
								newTeam.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											// Create Java Object
											final Team javaTeam = new Team(newTeam.getString("name"), currentUser.getUsername(), getResources().getDrawable(R.drawable.default_team_picture_thumb));
											teams.add(javaTeam);
											// Add relation with current user
											ParseRelation<ParseObject> teamsRelation = newTeam.getRelation("players");
											teamsRelation.add(currentUser);
											newTeam.saveInBackground(new SaveCallback() {
												@Override
												public void done(ParseException e) {
													if (e == null) {
														// Add javaPlayer
														ParseFile avatarFile = currentUser.getParseFile("avatar");
														
														try {
															byte[] avatarByteArray = avatarFile.getData();
															Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
															avatarBitmap = Bitmap.createScaledBitmap(avatarBitmap, 110, 110, false);
															javaTeam.addPlayer(new Player(currentUser.getUsername(), avatarBitmap));
														} catch (ParseException e1) {
															e1.printStackTrace();
														}
														
														// Update view
														expandableList.setAdapter(teamAdapter);
													}
												}
											});
										}
									}
								});
								teamName.setText("");
							} else{
								Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(getActivity(), "selected team : "+ teamAdapter.getSelectedTeam().getName(), Toast.LENGTH_SHORT).show();
				} 
				// If no team has been selected
				else {
					Toast.makeText(getActivity(), "Ooops! You must select a team to play." , Toast.LENGTH_SHORT).show();
				}
			}
		});
		
    	return mainView;
	}
	
	private void loadTeams(){
		// Parse query
		ParseQuery<ParseObject> teamsQuery = ParseQuery.getQuery("Team");
		teamsQuery.whereEqualTo("players", currentUser);
		teamsQuery.include("createdBy");
		teamsQuery.findInBackground(new FindCallback<ParseObject>() {
			// Find current user's teams with Parse
		    public void done(List<ParseObject> teamsList, ParseException e) {
		    	if( e != null ) {
		    		Toast.makeText(getActivity(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	createTeams(teamsList);
	        }
		});
	}

	private void createTeams(List<ParseObject> parseTeams) {
		for (ParseObject team : parseTeams) {
			// Create java Team
			ParseObject creator = new ParseObject("User");
			creator = team.getParseObject("createdBy");
			final Team newTeam = new Team(team.getString("name"), creator.getString("username"), getActivity().getResources().getDrawable(R.drawable.default_team_picture_thumb));
			// Get players of the team with Parse
			team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> players, ParseException e) {
					if(e != null) {
						Toast.makeText(getActivity(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
						return;
					}
					addPlayersToTeam( newTeam, players );
				}
			});
			teams.add(newTeam);
		}
	}
	
	private void addPlayersToTeam(Team team, List<ParseObject> players) {
		for (ParseObject player : players) {
			// Create small avatar
	    	ParseFile avatarFile = (ParseFile) player.get("avatar");
	    	try {
	    		byte[] avatarByteArray = avatarFile.getData();
				Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
				avatarBitmap = Bitmap.createScaledBitmap(avatarBitmap, 110, 110, false);
	
				// Add java Players
				team.addPlayer(new Player(((ParseUser) player).getUsername(), avatarBitmap));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		expandableList.setAdapter(teamAdapter);
	}
}