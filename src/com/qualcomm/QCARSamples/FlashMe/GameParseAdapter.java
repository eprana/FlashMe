package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class GameParseAdapter extends ParseQueryAdapter<ParseObject>{

	ParseUser user;
	
	public GameParseAdapter(Context context, final ParseObject user) {
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				// Define queries
				ParseQuery<ParseObject> gamesQuery = user.getRelation("games").getQuery();
				ParseQuery<ParseObject> createdGamesQuery = ParseQuery.getQuery("Game");
				createdGamesQuery.whereEqualTo("createdBy", user);
				// Compound queries
				List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
				queries.add(gamesQuery);
				queries.add(createdGamesQuery);
				return ParseQuery.or(queries);
			}
		});
		this.user = (ParseUser) user;
	}
	
	public void refresh() {
		this.loadObjects();
	}

	@Override
	public View getItemView(final ParseObject game, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.general_list, null);
		}
 
		super.getItemView(game, v, parent);
 
		TextView gameName = (TextView) v.findViewById(R.id.elem_name);
		gameName.setText(game.getString("name"));
		
		TextView gameCreator = (TextView) v.findViewById(R.id.elem_creator);
		String s_gameCreator = "";
		try {
			s_gameCreator = game.getParseUser("createdBy").fetchIfNeeded().getUsername();
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		gameCreator.setText(s_gameCreator);
		
		// Delete team button
		ImageButton deleteGame = (ImageButton)v.findViewById(R.id.delete_bt);
		deleteGame.setFocusable(false);
		
		if(!s_gameCreator.equals(user.getUsername())){
			deleteGame.setEnabled(false);
			deleteGame.setVisibility(View.INVISIBLE);
		}
		else {
			deleteGame.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
					alertDialog.setTitle(game.getString("name"));
					alertDialog.setMessage("Are you sure you want to delete this game ?");
					alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User wants to delete game
							game.deleteInBackground(new DeleteCallback() {
									
								@Override
								public void done(ParseException e) {
									refresh();
								}
							});
						}
					});
					alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled
						}
					});
					alertDialog.create();
					alertDialog.show();
				}
			});
		}
		
		return v;
	}
	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetInvalidated() {
		// TODO Auto-generated method stub
		super.notifyDataSetInvalidated();
	}
}
