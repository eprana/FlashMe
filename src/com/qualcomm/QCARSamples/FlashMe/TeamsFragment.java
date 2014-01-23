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
import android.app.Fragment;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TeamsFragment extends ListFragment {
	
	private OnTeamSelectedListener listener;
	
	public static String TAG="TAG_TEAMS";
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private TeamParseAdapter teamParseAdapter;
	private ListView teamsList;
	private EditText teamName;
	private Button createTeam;
	//private Button playButton;
	
	public interface OnTeamSelectedListener {
		public void onTeamSelected(int index);
	}
	
	public void setOnTeamSelectedListener(OnTeamSelectedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onTeamSelected(position);
		super.onListItemClick(l, v, position, id);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.teams, container, false);
		Context context = mainView.getContext();
		
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
    	teamName = (EditText) mainView.findViewById(R.id.enter_team);
    	createTeam = (Button) mainView.findViewById(R.id.create_team);
    	//playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
    	teamsList = (ListView) mainView.findViewById(R.id.teams_list);
    	teamParseAdapter = new TeamParseAdapter(getActivity(), currentUser);
    	teamsList.setAdapter(teamParseAdapter);
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
	
	/*private void createJavaTeam(ParseObject parseTeam) {
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
	}*/
}