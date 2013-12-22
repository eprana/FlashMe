package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ELVGameAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Game> games;
	private LayoutInflater inflater;
	
	public ELVGameAdapter(Context context, ArrayList<Game> games){
		this.context = context;
		this.games = games;
		inflater = LayoutInflater.from(context);
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
		if(team.getReady() == true) state = context.getResources().getColor(R.color.blue);
		
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

		GroupViewHolder gholder;

		if (convertView == null) {
        	gholder = new GroupViewHolder();
        	convertView = inflater.inflate(R.layout.expandable_row, null);
        	gholder.state = (TextView)convertView.findViewById(R.id.state);
			gholder.name = (TextView)convertView.findViewById(R.id.name);
			gholder.creator = (TextView)convertView.findViewById(R.id.creator);        	
			convertView.setTag(gholder);
        } else {
        	gholder = (GroupViewHolder) convertView.getTag();
        }

		// finding out if the team is ready or not
		int state = Color.GRAY;
		if(games.get(gamePos).getReady() == true){
			state = context.getResources().getColor(R.color.blue);
		}
		
		gholder.state.setBackgroundColor(state);
		gholder.state.setTextColor(state);
		gholder.name.setText(games.get(gamePos).getName());
		gholder.creator.setText(games.get(gamePos).getCreator());
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
	
	class GroupViewHolder {
		public TextView state;
		public TextView name;
		public TextView creator;
	}

	class ChildViewHolder {
		public TextView state;
		public TextView name;
		public ImageView picture;
	}

}
