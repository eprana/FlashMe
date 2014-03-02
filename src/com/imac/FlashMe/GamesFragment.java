package com.imac.FlashMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.imac.FlashMe.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
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

public class GamesFragment extends ListFragment {

	private static final String LOGTAG = "GamesFragment";
	
	// Data elements
	private ParseUser currentUser = null;
	private static ProgressBar progress = null;
	private static List<String> teamsList = null;
	private int state; // 0:games, 1:detail
	private String gameId;
	
	// Layout elements
	private GameParseAdapter gameParseAdapter;
	private GameTeamsParseAdapter gameTeamsParseAdapter;
	private ArrayAdapter<String> teamsAdapter;
	private ImageButton refreshButton;
	private EditText inputValue;
	private AutoCompleteTextView autocompleteValue;
	private Button createButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_games, container, false);
		final Context context = mainView.getContext();
    	
		// Initialize members
		state = 0;
		currentUser = ParseUser.getCurrentUser();
		teamsList = new ArrayList<String>();
		
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		refreshButton = (ImageButton) mainView.findViewById(R.id.refresh_bt);
    	inputValue = (EditText) mainView.findViewById(R.id.enter_game);

		createButton = (Button) mainView.findViewById(R.id.create_game);
    	
    	// Load fragment data
    	gameParseAdapter = new GameParseAdapter(context, currentUser);
    	gameParseAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {

			@Override
			public void onLoaded(List<ParseObject> arg0, Exception arg1) {
				progress.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);
			}

    	});
    	setListAdapter(gameParseAdapter);
    	
    	refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state) {
				case 0:
					gameParseAdapter.loadObjects();
					break;
				case 1:
					gameTeamsParseAdapter.loadObjects();
					break;
				default:
					break;
				}
				
			}
		});

    	// Create game button listener
    	createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String s_inputValue = inputValue.getText().toString();
				if(s_inputValue.equals("")){
					// Empty edit text
					Toast.makeText(context, R.string.empty_game_name, Toast.LENGTH_LONG).show();
					return;
				}
				else {
					createGame(s_inputValue);
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
			ParseObject game = ((ParseObject) l.getItemAtPosition(position));
			gameId = game.getObjectId();
			// Start detail activity
			final Intent intent = new Intent(getActivity(), GameTeamsActivity.class);
			intent.putExtra("GAME_ID", gameId);
			startActivity(intent);

		}
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
					newGame.put("state", 0);
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
}