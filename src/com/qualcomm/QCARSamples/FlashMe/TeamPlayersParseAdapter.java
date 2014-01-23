package com.qualcomm.QCARSamples.FlashMe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;

public class TeamPlayersParseAdapter extends ParseQueryAdapter<ParseObject>{

	public TeamPlayersParseAdapter(Context context, final ParseObject team) {
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseRelation<ParseObject> playersQuery = team.getRelation("players");
				return playersQuery.getQuery();
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
 
		TextView teamName = (TextView) v.findViewById(R.id.team_name);
		
		return v;
	}
}