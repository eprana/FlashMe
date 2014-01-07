package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ELVGameAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Game> games;
	private LayoutInflater inflater;
	private Game selectedGame;
	private int lastSelectedPosition;
	private CompoundButton lastSelectedButton;
	
	public ELVGameAdapter(Context context, ArrayList<Game> games){
		this.context = context;
		this.games = games;
		this.inflater = LayoutInflater.from(context);
		this.selectedGame = null;
		this.lastSelectedPosition = -1;
		this.lastSelectedButton = null;
	}
	
	@Override
	public boolean areAllItemsEnabled(){
		return true;
	}
	
	@Override
	public Object getChild(int gamePos, int teamPos) {
		return games.get(gamePos).getTeams().get(teamPos);
	}

	@Override
	public long getChildId(int gamePos, int teamPos) {
		return teamPos;
	}

	@Override
	public View getChildView(int gamePos, int teamPos, boolean isLastChild, View convertView, ViewGroup parent) {

		final Team team = (Team) getChild(gamePos, teamPos);
		ChildViewHolder childViewHolder;
		
		if (convertView == null) {
        	childViewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.game_team, null);
            childViewHolder.state = (TextView)convertView.findViewById(R.id.team_state);
            childViewHolder.name = (TextView)convertView.findViewById(R.id.team_name);
            childViewHolder.picture = (ImageView)convertView.findViewById(R.id.team_pic);
            convertView.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) convertView.getTag();	
		}
		
		// finding out if the player is ready or not
		int state = Color.GRAY;
		if(team.getReady() == true) state = context.getResources().getColor(R.color.middle_blue);
		
		childViewHolder.state.setBackgroundColor(state);
		childViewHolder.state.setTextColor(state);
		childViewHolder.name.setText(team.getName());
		childViewHolder.picture.setImageDrawable(team.getPicture());
		      
		return convertView;
	}

	@Override
	public int getChildrenCount(int gamePos) {
		return games.get(gamePos).getTeams().size();
	}

	@Override
	public Object getGroup(int gamePos) {
		return games.get(gamePos);
	}

	@Override
	public int getGroupCount() {
		return games.size();
	}

	@Override
	public long getGroupId(int gamePos) {
		return gamePos;
	}

	@Override
	public View getGroupView(int gamePos, boolean isExpanded, View convertView, ViewGroup parent) {

		final int gamePosition = gamePos;
		
		GroupViewHolder gholder;

		if (convertView == null) {
        	gholder = new GroupViewHolder();
        	convertView = inflater.inflate(R.layout.expandable_row, null);
        	gholder.state = (TextView)convertView.findViewById(R.id.state);
			gholder.name = (TextView)convertView.findViewById(R.id.name);
			gholder.creator = (TextView)convertView.findViewById(R.id.creator);
			gholder.selected = (CheckBox)convertView.findViewById(R.id.select_team);
			gholder.delete_bt = (ImageButton)convertView.findViewById(R.id.delete_team_bt);
			convertView.setTag(gholder);
        } else {
        	gholder = (GroupViewHolder) convertView.getTag();
        }

		// finding out if the team is ready or not
		int state = Color.GRAY;
		if(games.get(gamePos).getReady() == true){
			state = context.getResources().getColor(R.color.middle_blue);
		}
		
		gholder.state.setBackgroundColor(state);
		gholder.state.setTextColor(state);
		gholder.name.setText(games.get(gamePos).getName());
		gholder.creator.setText(games.get(gamePos).getCreator());	
		
		// Delete a game
		gholder.delete_bt.setFocusable(false);
		gholder.delete_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get concerned team with Parse
				ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Game");
				teamQuery.whereEqualTo("name", ((Game) getGroup(gamePosition)).getName());
				teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(final ParseObject game, ParseException e) {
						if (e==null){
							// Remove Team in Parse
							game.deleteInBackground();
							// Remove java object
							games.remove(gamePosition);
							// Display success message
							Toast.makeText(context, "You just deleted the game "+game.getString("name")+".", Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(context, "The game can't be deleted.", Toast.LENGTH_SHORT).show();
						}
						
					}
				});
			}
		});
		
		// Choose a team to start a game
		gholder.selected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					// Forbid to chose several teams to play
					if(lastSelectedPosition != -1 && gamePosition != lastSelectedPosition) {
						lastSelectedButton.setChecked(false);
					}
					lastSelectedPosition = gamePosition;
					lastSelectedButton = buttonView;
					selectedGame = games.get(gamePosition);
				} else {
					lastSelectedPosition = -1;
					lastSelectedButton = null;
					selectedGame = null;
				}
			}
		});
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int gamePos, int teamPos) {
		return false;
	}
	
	public Game getSelectedGame(){
		return selectedGame;
	}
	
	class GroupViewHolder {
		public TextView state;
		public TextView name;
		public TextView creator;
		public CheckBox selected;
		public ImageButton delete_bt;
	}

	class ChildViewHolder {
		public TextView state;
		public TextView name;
		public ImageView picture;
	}

}
