package com.qualcomm.QCARSamples.FlashMe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class TeamParseAdapter extends ParseQueryAdapter<ParseObject>{
	
	public TeamParseAdapter(Context context, final ParseObject user) {

		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseQuery<ParseObject> teamsQuery = new ParseQuery<ParseObject>("Team");
				teamsQuery.whereEqualTo("players", user);
				return teamsQuery;
			}
		});
	}
	
	public void refresh() {
		this.loadObjects();
	}
	
	@Override
	public View getItemView(final ParseObject team, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.teams_list, null);
		}
 
		super.getItemView(team, v, parent);
		
		// Set team data
		TextView teamName = (TextView) v.findViewById(R.id.team_name);
		teamName.setText(team.getString("name"));
		TextView teamCreator = (TextView) v.findViewById(R.id.team_creator);
		try {
			teamCreator.setText(team.getParseUser("createdBy").fetchIfNeeded().getUsername());
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		// Team details button
		ImageButton details = (ImageButton)v.findViewById(R.id.details_bt);
		details.setFocusable(false);
		details.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Bundle args = new Bundle();
//				args.putString("team", team.getString("name"));
//				if(getContext() instanceof ContentActivity)
//				{
//					ContentActivity activity = (ContentActivity) getContext();
//					Fragment teamPlayersFragment = new TeamPlayersFragment();
//					FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//					ft.add(R.id.pager, teamPlayersFragment).commit();
//				}
			}
		});
		
		// Delete team button
		ImageButton deleteTeam = (ImageButton)v.findViewById(R.id.delete_bt);
		deleteTeam.setFocusable(false);
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
				alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled
					}
				});
				alertDialog.create();
				alertDialog.show();
			}
		});
		
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
