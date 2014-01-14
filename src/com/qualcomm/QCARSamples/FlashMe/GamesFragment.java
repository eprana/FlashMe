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

public class GamesFragment extends Fragment {

	// Data elements
	private ParseUser currentUser = null;
	private static ArrayList<Game> games = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private static ELVGameAdapter gameAdapter;
	private static ExpandableListView expandableList = null;
	private EditText gameName;
	private Button createGame;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.games, container, false);
		final Context context = mainView.getContext();
    	
		// Get current user
		currentUser = ParseUser.getCurrentUser();

    	// Initialize games ArrayList
    	games = new ArrayList<Game>();
    	
    	// Initialize expandable list
    	gameAdapter = new ELVGameAdapter(context, games);        	
    	expandableList = (ExpandableListView) mainView.findViewById(R.id.games_list);
    	
    	// Load fragment data
        progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
    	LoadTeams lt = new LoadTeams(context);
    	lt.execute();

    	// Create game button
		gameName = (EditText) mainView.findViewById(R.id.enter_game);
		createGame = (Button) mainView.findViewById(R.id.create_game);
		createGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_gameName = gameName.getText().toString();
				if(s_gameName.equals("")){
					// Empty edit text
					Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_LONG).show();
					return;
				}
				else {
					createGame(s_gameName);
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
	    	loadGames(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);
			expandableList.setAdapter(gameAdapter);
		}
	}
	private static void loadGames(final Context context) {
		// Parse query
		ParseQuery<ParseObject> gamesQuery = ParseQuery.getQuery("Game");
		gamesQuery.findInBackground(new FindCallback<ParseObject>() {
			// Find all existing games with Parse
		    public void done(List<ParseObject> gamesList, ParseException e) {
		        if (e != null) {
		        	Toast.makeText(context, "Error : " + e.toString(), Toast.LENGTH_LONG).show();
		        	return;
		        }
		        createGames(context, gamesList);
		    }
		});
	}
	
	private static void createGames(final Context context, List<ParseObject> gamesList) {
		for (final ParseObject game : gamesList) {
			// Create java Game
			try {
				final Game newGame = new Game(game.getString("name"), ((ParseUser) game.fetch().getParseObject("createdBy")).fetch().getUsername());
	
				// Get teams in game with Parse
	    		game.getRelation("teams").getQuery().findInBackground(new FindCallback<ParseObject>() {
	    			public void done(List<ParseObject> teamsList, ParseException e) {
	    				if (e != null) {
	    					Toast.makeText(context, "Error : " + e.toString(), Toast.LENGTH_LONG).show();
	    					return;
	    				}
	    				addTeamsToGame(context, newGame, teamsList);
					}
				});
	    		games.add(newGame);
			} catch (NotFoundException e1) {
				Toast.makeText(context, "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			} catch (ParseException e1) {
				Toast.makeText(context, "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			}
		}
	}
	
	private static void addTeamsToGame(Context context, Game game, List<ParseObject> teamsList) {
		for (ParseObject team : teamsList) {
			ParseObject creator = new ParseObject("User");
			creator = team.getParseObject("createdBy");
			try {
				// Add new java Team
				game.addTeam(new Team(team.fetch().getString("name"), creator.fetch().getString("username"), context.getResources().getDrawable(R.drawable.default_team_picture_thumb)));
			} catch (NotFoundException e1) {
				Toast.makeText(context, "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			} catch (ParseException e1) {
				Toast.makeText(context, "Error : " + e1.toString(), Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			}
		}
	}
	
	// Create game button
	private void createGame(final String name) {
		ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
		gameQuery.whereEqualTo("name", name);
		gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			// Check if a game already exists with the same name
			public void done(ParseObject arg0, ParseException e) {
				if(e==null){
					Toast.makeText(getActivity(), "Sorry, this name has already be taken.", Toast.LENGTH_LONG).show();
				}else if(e.getCode() == 101){
					// Create Parse game
					final ParseObject newGame = new ParseObject("Game");
					newGame.put("name", name);
					newGame.put("createdBy", currentUser);
					newGame.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e != null) {
								Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
								return;
							}
							// Create Java game
							games.add(new Game(newGame.getString("name"), currentUser.getUsername()));
							expandableList.setAdapter(gameAdapter);
						}
					});
					// Clear edit text value
					gameName.setText("");
				} else{
					Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}