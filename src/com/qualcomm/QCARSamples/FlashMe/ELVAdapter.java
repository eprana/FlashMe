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

public class ELVAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Team> teams;
	private LayoutInflater inflater;
	
	public ELVAdapter(Context context, ArrayList<Team> teams){
		this.context = context;
		this.teams = teams;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public boolean areAllItemsEnabled(){
		return true;
	}
	
	@Override
	public Object getChild(int teamPos, int playerPos) {
		return teams.get(teamPos).getPlayers().get(playerPos);
	}

	@Override
	public long getChildId(int teamPos, int playerPos) {
		return playerPos;
	}

	@Override
	public View getChildView(int teamPos, int playerPos, boolean isLastChild, View convertView, ViewGroup parent) {

		final Player player = (Player) getChild(teamPos, playerPos);
		ChildViewHolder childViewHolder;
		
		if (convertView == null) {
        	childViewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.team_player, null);
            childViewHolder.state = (TextView)convertView.findViewById(R.id.player_state);
            childViewHolder.name = (TextView)convertView.findViewById(R.id.player_name);
            childViewHolder.picture = (ImageView)convertView.findViewById(R.id.player_pic);
            convertView.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) convertView.getTag();	
		}
		
		// finding out if the player is ready or not
		int state = Color.GRAY;
		if(player.getReady() == true) state = context.getResources().getColor(R.color.blue);
		
		childViewHolder.state.setBackgroundColor(state);
		childViewHolder.state.setTextColor(state);
		childViewHolder.name.setText(player.getName());
		childViewHolder.picture.setImageDrawable(player.getPicture());
		      
		return convertView;
	}

	@Override
	public int getChildrenCount(int teamPos) {
		return teams.get(teamPos).getPlayers().size();
	}

	@Override
	public Object getGroup(int teamPos) {
		return teams.get(teamPos);
	}

	@Override
	public int getGroupCount() {
		return teams.size();
	}

	@Override
	public long getGroupId(int teamPos) {
		return teamPos;
	}

	@Override
	public View getGroupView(int teamPos, boolean isExpanded, View convertView, ViewGroup parent) {

		GroupViewHolder gholder;

		if (convertView == null) {
        	gholder = new GroupViewHolder();
        	convertView = inflater.inflate(R.layout.team_row, null);
        	gholder.state = (TextView)convertView.findViewById(R.id.team_state);
			gholder.name = (TextView)convertView.findViewById(R.id.team_name);
			gholder.creator = (TextView)convertView.findViewById(R.id.team_creator);        	
			convertView.setTag(gholder);
        } else {
        	gholder = (GroupViewHolder) convertView.getTag();
        }

		// finding out if the team is ready or not
		int state = Color.GRAY;
		if(teams.get(teamPos).getReady() == true){
			state = context.getResources().getColor(R.color.blue);
		}
		
		gholder.state.setBackgroundColor(state);
		gholder.state.setTextColor(state);
		gholder.name.setText(teams.get(teamPos).getName());
		gholder.creator.setText(teams.get(teamPos).getCreator());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int teamPos, int playerPos) {
		return true;
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
