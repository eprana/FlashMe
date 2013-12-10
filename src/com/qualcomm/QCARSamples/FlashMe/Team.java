package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class Team {
	
	private boolean ready;
	private String name;
	private String creator;
	private ArrayList<Player> players;
	
	public Team(String name, String creator){
		super();
		this.ready = false;
		this.name = name;
		this.creator = creator;
		this.players = new ArrayList<Player>();
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
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(ArrayList<Player> players){
		this.players = players;
	}
	
	public void addPlayer(Team team, String name, Drawable picture){
		Player player = new Player(team, name, picture);
		players.add(player);
	}
	
}
