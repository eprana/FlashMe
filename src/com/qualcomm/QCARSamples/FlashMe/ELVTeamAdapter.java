package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.storage.OnObbStateChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ELVTeamAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Team> teams;
	private LayoutInflater inflater;
	private Team selectedTeam;
	private int lastSelectedPosition;
	private CompoundButton lastSelectedButton;
	
	public ELVTeamAdapter(Context context, ArrayList<Team> teams){
		this.context = context;
		this.teams = teams;
		this.inflater = LayoutInflater.from(context);
		this.selectedTeam = null;
		this.lastSelectedPosition = -1;
		this.lastSelectedButton = null;
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
		if(player.getReady() == true) state = context.getResources().getColor(R.color.middle_blue);
		
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

		final int teamPosition = teamPos;
		
		GroupViewHolder gholder;
		
		if (convertView == null) {
        	gholder = new GroupViewHolder();
        	convertView = inflater.inflate(R.layout.expandable_row, null);
        	gholder.state = (TextView)convertView.findViewById(R.id.state);
			gholder.name = (TextView)convertView.findViewById(R.id.name);
			gholder.creator = (TextView)convertView.findViewById(R.id.creator);
			gholder.selected = (CheckBox)convertView.findViewById(R.id.select_team);
			convertView.setTag(gholder);
        } else {
        	gholder = (GroupViewHolder) convertView.getTag();
        }

		// Finding out if the team is ready or not
		int state = Color.GRAY;
		if(teams.get(teamPos).getReady() == true){
			state = context.getResources().getColor(R.color.middle_blue);
		}
		
		gholder.state.setBackgroundColor(state);
		gholder.state.setTextColor(state);
		gholder.name.setText(teams.get(teamPos).getName());
		gholder.creator.setText(teams.get(teamPos).getCreator());
		
		// Choosing a team to play
		gholder.selected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					// Forbid to chose several teams to play
					if(lastSelectedPosition != -1 && teamPosition != lastSelectedPosition) {
						lastSelectedButton.setChecked(false);
					}
					lastSelectedPosition = teamPosition;
					lastSelectedButton = buttonView;
					selectedTeam = teams.get(teamPosition);
				} else {
					lastSelectedPosition = -1;
					lastSelectedButton = null;
					selectedTeam = null;
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
	public boolean isChildSelectable(int teamPos, int playerPos) {
		return false;
	}
	
	public Team getSelectedTeam(){
		return selectedTeam;
	}
	
	class GroupViewHolder {
		public TextView state;
		public TextView name;
		public TextView creator;
		public CheckBox selected;
	}

	class ChildViewHolder {
		public TextView state;
		public TextView name;
		public ImageView picture;
	}

}
