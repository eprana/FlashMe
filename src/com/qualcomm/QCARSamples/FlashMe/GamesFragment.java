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
import com.parse.ParseQueryAdapter.OnQueryLoadListener;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GamesFragment extends ListFragment {

	// Data elements
	private ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private int state; // 0:games, 1:detail
	private String gameName;
	
	// Layout elements
	private GameParseAdapter gameParseAdapter;
	private GameTeamsParseAdapter gameTeamsParseAdapter;
	private ImageButton backButton;
	private EditText inputValue;
	private Button addButton;
	private Button playButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.games, container, false);
		final Context context = mainView.getContext();
    	
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
    	
		backButton = (ImageButton) mainView.findViewById(R.id.back_bt);
    	inputValue = (EditText) mainView.findViewById(R.id.enter_game);
		addButton = (Button) mainView.findViewById(R.id.create_game);
		playButton = (Button) mainView.findViewById(R.id.play);
    	
    	// Load fragment data
    	gameParseAdapter = new GameParseAdapter(context, currentUser);
    	setListAdapter(gameParseAdapter);
    	gameParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {

			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.GONE);
			}

			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}

    	});

    	// Create game button listener
    	addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_inputValue = inputValue.getText().toString();
				if(s_inputValue.equals("")){
					// Empty edit text
					Toast.makeText(context, R.string.empty_team_name, Toast.LENGTH_LONG).show();
					return;
				}
				else {
					switch(state) {
					case 0:
						createGame(s_inputValue);
						break;
					case 1:
						addTeamToGame(s_inputValue);
						break;
					default:
						break;
					}
				}
			}
		});

		// Create play button listener
//		playButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(gameAdapter.getSelectedGame() != null){
//					Toast.makeText(context, "Selected team : "+ gameAdapter.getSelectedGame().getName(), Toast.LENGTH_SHORT).show();
//				} 
//				// If no team has been selected
//				else {
//					Toast.makeText(context, "Ooops! You must select a game to play." , Toast.LENGTH_SHORT).show();
//				}
//			}
//		});

    	return mainView;	
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		
		ParseObject game = ((ParseObject) l.getItemAtPosition(position));
		gameName = game.getString("name");
		gameTeamsParseAdapter = new GameTeamsParseAdapter(getActivity(), currentUser, game);
		setDetailAdapter(gameTeamsParseAdapter);
	}
	
	public void setGeneralAdapter() {
		state = 0;
		gameName= "";
		inputValue.setHint("New game name");
		addButton.setText("CREATE");		
		backButton.setVisibility(View.INVISIBLE);
		
		setListAdapter(gameParseAdapter);
	}
	
	public void setDetailAdapter(GameTeamsParseAdapter gameTeamsParseAdapter) {
		state = 1;
		inputValue.setHint("Game name");
		addButton.setText("ADD");	
		backButton.setVisibility(View.VISIBLE);
		
		gameTeamsParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.GONE);
			}
			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}
    	});
		setListAdapter(gameTeamsParseAdapter);
	}
	
	// Create game
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
							currentUser.getRelation("games").add(newGame);
							currentUser.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException e) {
									if (e == null) {
										gameParseAdapter.loadObjects();
									}
								}
							});
						}
					});
					// Clear edit text value
					inputValue.setText("");
				} else{
					Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	// Add player to team
		private void addTeamToGame(final String teamName) {
			// Parse query
			ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
			gameQuery.whereEqualTo("name", gameName);
			// Get concerned team
			gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
				@Override
				public void done(final ParseObject game, ParseException e) {
					if(e!=null){
						Toast.makeText(getActivity(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
						return;
					}
					// Parse Query
					ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
					teamQuery.whereEqualTo("username", teamName);
					// Get selected player
					teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject team, ParseException e) {
							if(e!=null){
								Toast.makeText(getActivity(), "Sorry, this team doesn't exist.", Toast.LENGTH_SHORT).show();
								return;
							}
							team.getRelation("teams").add(team);
							team.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException e) {
									if (e == null) {
										gameTeamsParseAdapter.loadObjects();
									}
								}
							});
							inputValue.setText("");
						}
					});
				}
			});
		}
}