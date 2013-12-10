package com.qualcomm.QCARSamples.FlashMe;

import android.graphics.drawable.Drawable;

public class Player {

	private Team team;
	private boolean ready;
	private String name;
	private Drawable picture;
	
	public Player(Team team, String name, Drawable picture){
		super();
		this.team = team;
		this.ready = false;
		this.name = name;
		this.picture = picture;
	}
	
	public Team getTeam(){
		return team;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean getReady(){
		return ready;
	}
	
	public Drawable getPicture(){
		return picture;
	}	
	
	public void setReady(boolean ready){
		this.ready = ready;
	}

	public void setPicture(Drawable picture){
		this.picture = picture;
	}
	
}