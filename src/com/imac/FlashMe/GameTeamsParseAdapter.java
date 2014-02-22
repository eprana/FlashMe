package com.imac.FlashMe;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.imac.FlashMe.R;

public class GameTeamsParseAdapter extends ParseQueryAdapter<ParseObject>{
	
	ParseObject game;
	ParseUser user;

	public GameTeamsParseAdapter(Context context, final ParseObject user, final ParseObject game) {
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseRelation<ParseObject> playersQuery = game.getRelation("teams");
				return playersQuery.getQuery();
			}
		});
		this.game = game;
		this.user = (ParseUser) user;
	}
	
	public void refresh() {
		this.loadObjects();
	}
	
	public boolean isCreator(){
		String s_gameCreator = "";
		try {
			s_gameCreator = game.getParseUser("createdBy").fetchIfNeeded().getUsername();
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		if(s_gameCreator.equals(user.getUsername())){
			return true;
		}
		return false;
	}
	
	@Override
	public View getItemView(final ParseObject team, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.details_list, null);
		}
 
		super.getItemView(team, v, parent);
 
		TextView teamName = (TextView) v.findViewById(R.id.elem_name);
		teamName.setText(team.getString("name"));
		
		TextView teamCreator = (TextView) v.findViewById(R.id.elem_detail);
		String s_teamCreator = "";
		try {
			s_teamCreator = team.getParseUser("createdBy").fetchIfNeeded().getUsername();
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		teamCreator.setText(s_teamCreator);
		
		final ImageView teamState = (ImageView) v.findViewById(R.id.elem_state);
		ParseRelation<ParseObject> teamPlayers = team.getRelation("players");
		teamPlayers.getQuery().findInBackground(new FindCallback<ParseObject>() {
			boolean teamIsReady = true;
			@Override
			public void done(List<ParseObject> players, ParseException e) {
				for (ParseObject player : players) {
					if (e!=null) {
						Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
						return;
					}
					if (player.getInt("state") == 0) {
						teamIsReady = false;
					}
				}
				if(teamIsReady) {
					teamState.setImageResource(R.drawable.blue_rectangle);
				}
			}
		});
		
		//final ImageView teamPicture = (ImageView) v.findViewById(R.id.elem_picture);
		
		String s_gameCreator = "";
		try {
			s_gameCreator = game.getParseUser("createdBy").fetchIfNeeded().getUsername();
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		// Delete game button
		ImageButton deleteTeam = (ImageButton)v.findViewById(R.id.delete_bt);
		
		if(!s_gameCreator.equals(user.getUsername()) && !s_teamCreator.equals(user.getUsername())){
			deleteTeam.setEnabled(false);
			deleteTeam.setImageResource(R.drawable.delete_bt);
		}
		else {
			deleteTeam.setEnabled(true);
			deleteTeam.setImageResource(R.drawable.blue_delete_bt);
			deleteTeam.setFocusable(false);
			deleteTeam.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
					alertDialog.setTitle(team.getString("name"));
					alertDialog.setMessage("Are you sure you want to delete the team "+team.getString("name")+" from this game ?");
					alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User wants to delete team
							game.getRelation("teams").remove(team);
							game.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException e) {
									team.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
										@Override
										public void done(List<ParseObject> players, ParseException e) {
											for(ParseObject player : players) {
												HashMap<String, Object> params = new HashMap<String, Object>();
												params.put("userId", player.getObjectId());
												params.put("gameId", game.getObjectId());
												ParseCloud.callFunctionInBackground("removeGameFromUser", params, new FunctionCallback<String>() {
													public void done(String result, ParseException e) {
														if (e != null) {
															Toast.makeText(getContext(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
														}
													}
												});
											}
											
										}
									});
									refresh();								
								}
							});
						}
					});
					alertDialog.setNegativeButton("CANCEL", null);
					alertDialog.create();
					alertDialog.show();
				}
			});
		}		
		return v;
	}
}
