package com.qualcomm.QCARSamples.FlashMe;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class GameParseAdapter extends ParseQueryAdapter<ParseObject>{

	public GameParseAdapter(Context context, final ParseObject user) {
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseQuery<ParseObject> teamsQuery = new ParseQuery<ParseObject>("Game");
				//teamsQuery.whereEqualTo("players", user);
				return teamsQuery;
			}
		});
	}
	
	@Override
	public View getItemView(ParseObject game, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.games_list, null);
		}
 
		super.getItemView(game, v, parent);
 
		TextView gameName = (TextView) v.findViewById(R.id.game_name);
		gameName.setText(game.getString("name"));
		
		TextView teamCreator = (TextView) v.findViewById(R.id.game_creator);
		try {
			teamCreator.setText(game.getParseUser("createdBy").fetchIfNeeded().getUsername());
		} catch (ParseException e) {
			Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		return v;
	}
}
