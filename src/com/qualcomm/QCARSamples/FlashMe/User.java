package com.qualcomm.QCARSamples.FlashMe;

import android.graphics.Bitmap;

public class User {

	private String username;
	private Bitmap profilePicture;
	private int state;
	private int totalScore;
	private int level;
	private int bestScore;
	private int rank;
	private int victories;
	private int defeats;
	
	public User(String username, Bitmap profilePicture) {
		this.setUsername(username);
		this.setProfilePicture(profilePicture);
		this.setState(0);
		this.setTotalScore(0);
		this.setLevel(0);
		this.setBestScore(0);
		this.setRank(0);
		this.setVictories(0);
		this.setDefeats(0);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Bitmap getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(Bitmap profilePicture) {
		this.profilePicture = profilePicture;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBestScore() {
		return bestScore;
	}

	public void setBestScore(int bestScore) {
		this.bestScore = bestScore;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getVictories() {
		return victories;
	}

	public void setVictories(int victories) {
		this.victories = victories;
	}

	public int getDefeats() {
		return defeats;
	}

	public void setDefeats(int defeats) {
		this.defeats = defeats;
	}

}
