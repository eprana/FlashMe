package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class Team {
	
	private boolean ready;
	private String name;
	private String creator;
	private ArrayList<Player> players;
	private Drawable picture;
	
	public Team(String name, String creator, Drawable picture){
		super();
		this.ready = false;
		this.name = name;
		this.creator = creator;
		this.players = new ArrayList<Player>();
		this.picture = picture;
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
	
	public Drawable getPicture(){
		return picture;
	}
	
	public void setPicture(Drawable picture){
		this.picture = picture;
	}
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(ArrayList<Player> players){
		this.players = players;
	}
	
	public void addPlayer(Player player){
		players.add(player);
	}
	
	public void removePlayer(Player player){
		players.remove(player);
	}
	
}
