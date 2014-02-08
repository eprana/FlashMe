package com.qualcomm.QCARSamples.FlashMe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
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
import com.parse.SaveCallback;

public class TeamPlayersParseAdapter extends ParseQueryAdapter<ParseObject>{
	
	ParseObject team;

	public TeamPlayersParseAdapter(Context context, final ParseObject user, final ParseObject team) {
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery<ParseObject> create() {
				ParseRelation<ParseObject> playersQuery = team.getRelation("players");
				return playersQuery.getQuery();
			}
		});
		this.team = team;
	}
	
	public void refresh() {
		this.loadObjects();
	}
	
	@Override
	public View getItemView(final ParseObject player, View v, ViewGroup parent) {
		
		if (v == null) {
			v = View.inflate(getContext(), R.layout.details_list, null);
		}
 
		super.getItemView(player, v, parent);
 
		TextView playerName = (TextView) v.findViewById(R.id.elem_name);
		playerName.setText(player.getString("username"));
		
		final ImageView playerPicture = (ImageView) v.findViewById(R.id.elem_picture);
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
		
		// Delete team button
		ImageButton deletePlayer = (ImageButton)v.findViewById(R.id.delete_bt);
		deletePlayer.setFocusable(false);
		deletePlayer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
				alertDialog.setTitle(team.getString("name"));
				alertDialog.setMessage("Are you sure you want to delete "+player.getString("username")+" from this team ?");
				alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User wants to delete team
						team.getRelation("players").remove(player);
						team.saveInBackground(new SaveCallback() {
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
}