package com.imac.FlashMe;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.imac.FlashMe.R;

public class TeamParseAdapter extends ParseQueryAdapter<ParseObject>{
	
	ParseUser user;
	
	public TeamParseAdapter(Context context, final ParseObject user) {

		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseQuery<ParseObject> teamsQuery = new ParseQuery<ParseObject>("Team");
				teamsQuery.whereEqualTo("players", user);
				return teamsQuery;
			}
		});
		this.user = (ParseUser) user;
	}
	
	public void refresh() {
		this.loadObjects();
	}
	
	@Override
	public View getItemView(final ParseObject team, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.general_list, null);
		}
 
		super.getItemView(team, v, parent);
		
		// Set team data
		TextView teamName = (TextView) v.findViewById(R.id.elem_name);
		teamName.setText(team.getString("name"));
		
		TextView teamCreator = (TextView) v.findViewById(R.id.elem_creator);
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

		// Delete team button
		ImageButton deleteTeam = (ImageButton)v.findViewById(R.id.delete_bt);
		deleteTeam.setFocusable(false);
		
		if(!s_teamCreator.equals(user.getUsername())){
			deleteTeam.setEnabled(false);
			deleteTeam.setImageResource(R.drawable.delete_bt);
		}
		else {
			deleteTeam.setEnabled(true);
			deleteTeam.setImageResource(R.drawable.blue_delete_bt);
			deleteTeam.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
					alertDialog.setTitle(team.getString("name"));
					alertDialog.setMessage("Are you sure you want to delete this team ?");
					alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User wants to delete team
							team.deleteInBackground(new DeleteCallback() {
									
								@Override
								public void done(ParseException e) {
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
