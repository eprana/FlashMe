package com.qualcomm.QCARSamples.FlashMe;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Player {

	private boolean ready;
	private String name;
	private Bitmap picture;
	
	public Player(String name, Bitmap picture){
		super();
		this.ready = false;
		this.name = name;
		this.picture = picture;
	}
	
//	public Team getTeam(){
//		return team;
//	}
	
	public String getName(){
		return name;
	}
	
	public boolean getReady(){
		return ready;
	}
	
	public Bitmap getPicture(){
		return picture;
	}	
	
	public void setReady(boolean ready){
		this.ready = ready;
	}

	public void setPicture(Bitmap picture){
		this.picture = picture;
	}
	
}