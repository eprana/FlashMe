package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class GamesFragment extends Fragment {

	// Data elements
	private ParseUser currentUser = null;
	private static ArrayList<Game> games = null;
	
	// Layout elements
	private static ELVGameAdapter gameAdapter;
	private static ExpandableListView expandableList = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.games, container, false);
		final Context context = mainView.getContext();
    	
		// Get current user
		currentUser = ParseUser.getCurrentUser();

    	// Initialize games ArrayList
    	games = new ArrayList<Game>();
    	
    	// Get user's teams with Parse to create Java Teams
    	this.loadGames();
    	
    	// Display teams in expandable list
    	gameAdapter = new ELVGameAdapter(context, games);        	
    	expandableList = (ExpandableListView) mainView.findViewById(R.id.games_list);
    	expandableList.setAdapter(gameAdapter);

    	// Create game button
		final EditText gameName = (EditText) mainView.findViewById(R.id.enter_game);
		Button createButton = (Button) mainView.findViewById(R.id.create_game);
		createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_gameName = gameName.getText().toString();
				if(s_gameName.equals("")){
					// Invalid game name
					Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_SHORT).show();
					return;
				}
				else {
					// Check if the name does not exist yet
					ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
					gameQuery.whereEqualTo("name", s_gameName);
					gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject arg0, ParseException e) {
							if(e==null){
								Toast.makeText(context, "Sorry, this name has already be taken.", Toast.LENGTH_SHORT).show();
							}else if(e.getCode() == 101){
								// Create game
								final ParseObject newGame = new ParseObject("Game");
								newGame.put("name", s_gameName);
								newGame.put("createdBy", currentUser);
								newGame.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											games.add(new Game(newGame.getString("name"), currentUser.getUsername()));
											expandableList.setAdapter(gameAdapter);
										}
									}
								});
								gameName.setText("");
							} else{
								Toast.makeText(context, "An error occured.", Toast.LENGTH_SHORT).show();
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
				if(gameAdapter.getSelectedGame() != null){
					Toast.makeText(context, "Selected team : "+ gameAdapter.getSelectedGame().getName(), Toast.LENGTH_SHORT).show();
				} 
				// If no team has been selected
				else {
					Toast.makeText(context, "Ooops! You must select a game to play." , Toast.LENGTH_SHORT).show();
				}
			}
		});
    	return mainView;
    	
	}
	
	private void loadGames() {
		// Parse query
		ParseQuery<ParseObject> gamesQuery = ParseQuery.getQuery("Game");
		gamesQuery.findInBackground(new FindCallback<ParseObject>() {
			// Find all existing games with Parse
		    public void done(List<ParseObject> gamesList, ParseException e) {
		        if (e != null) {
		        	Toast.makeText(getActivity(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
		        	return;
		        }
		        createGames(gamesList);
		    }
		});
	}
	
	private void createGames(List<ParseObject> gamesList) {
		for (final ParseObject game : gamesList) {
			// Create java Game
			try {
				final Game newGame = new Game(game.getString("name"), ((ParseUser) game.fetch().getParseObject("createdBy")).fetch().getUsername());
	
				// Get teams in game with Parse
	    		game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
	    			public void done(List<ParseObject> teamsList, ParseException e) {
	    				if (e == null) {
	    					for (ParseObject team : teamsList) {
	    						// Add java Teams
	    						ParseObject creator = new ParseObject("User");
	    						creator = team.getParseObject("createdBy");
								try {
									newGame.addTeam(new Team(team.getString("name"), creator.fetch().getString("username"), getActivity().getResources().getDrawable(R.drawable.default_team_picture_thumb)));
								} catch (NotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	    					}
	    				}
					}
				});
	    		games.add(newGame);
			} catch (NotFoundException e1) {
				Toast.makeText(getActivity(), "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			} catch (ParseException e1) {
				Toast.makeText(getActivity(), "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			}
		}
		expandableList.setAdapter(gameAdapter);
	}
}