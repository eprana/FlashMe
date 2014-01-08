package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

public class Game {

	private boolean ready;
	private String name;
	private String creator;
	private ArrayList<Team> teams;
	
	public Game(String name, String creator){
		super();
		this.ready = false;
		this.name = name;
		this.creator = creator;
		this.teams = new ArrayList<Team>();
	}
	
	public boolean getReady(){
		return ready;
	}
	
	public void setReady(boolean ready){
		this.ready = ready;
	}
	
	public String getName(){
		return name;
	}
	
	public String getCreator(){
		return creator;
	}
	
	public ArrayList<Team> getTeams(){
		return teams;
	}
	
	public void setTeams(ArrayList<Team> teams){
		this.teams = teams;
	}
	
	public void addTeam(Team team){
		teams.add(team);
	}
	
}
