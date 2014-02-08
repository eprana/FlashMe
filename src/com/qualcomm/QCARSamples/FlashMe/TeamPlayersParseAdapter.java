package com.qualcomm.QCARSamples.FlashMe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;

public class TeamPlayersParseAdapter extends ParseQueryAdapter<ParseObject>{

	public TeamPlayersParseAdapter(Context context, final ParseObject user, final ParseObject team) {
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
	public View getItemView(final ParseObject player, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.team_players_list, null);
		}
 
		super.getItemView(player, v, parent);
 
		TextView playerName = (TextView) v.findViewById(R.id.player_name);
		playerName.setText(player.getString("username"));
		
		final ImageView playerPicture = (ImageView) v.findViewById(R.id.player_picture);
		ParseFile file = (ParseFile) player.get("avatar");
		file.getDataInBackground(new GetDataCallback() {
			public void done(byte[] data, ParseException e) {
				if (e != null){
					Toast.makeText(getContext(), "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
					return;
				}
				Bitmap profilePicture = BitmapFactory.decodeByteArray(data, 0, data.length);
				playerPicture.setImageBitmap(profilePicture);
			}
		});
		
		return v;
	}
}