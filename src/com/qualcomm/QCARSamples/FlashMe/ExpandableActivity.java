package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

public class ExpandableActivity extends Activity {

	private ExpandableListView expandableList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teams);
		
		expandableList = (ExpandableListView) findViewById(R.id.teams_list);
		ArrayList<Team> teams = new ArrayList<Team>();

		//Teams tests
		Team team1 = createTeam("Anti-Heroes", "Zizi");
		team1.addPlayer(team1, "Zizi", getResources().getDrawable(R.drawable.pic_empty_mini));
		team1.addPlayer(team1, "Flo", getResources().getDrawable(R.drawable.pic_flo));
		team1.addPlayer(team1, "Xopi", getResources().getDrawable(R.drawable.pic_empty_mini));
		team1.setReady(true);
		
		Team team2 = createTeam("Hydro-Gène", "Flo");
		team2.addPlayer(team2, "Flo", getResources().getDrawable(R.drawable.pic_flo));
		team2.addPlayer(team2, "Jiji", getResources().getDrawable(R.drawable.pic_empty_mini));
		team2.addPlayer(team2, "Cédric", getResources().getDrawable(R.drawable.pic_empty_mini));
		team2.setReady(true);

		//Players are ready? tests
		team1.getPlayers().get(0).setReady(true);
		team1.getPlayers().get(2).setReady(true);
		team2.getPlayers().get(0).setReady(true);
		team2.getPlayers().get(1).setReady(true);
		team2.getPlayers().get(2).setReady(true);
		
		//Changing picture tests
		team1.getPlayers().get(2).setPicture(getResources().getDrawable(R.drawable.pic_xopi));
				
		//Add players to team
		teams.add(team1);
		teams.add(team2);
		
		ELVAdapter adapter = new ELVAdapter(this, teams);
		expandableList.setAdapter(adapter);
	}
	
	public Team createTeam(String name, String creator){
		return new Team(name, creator);
	}
	
}