package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
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
	private View alertDialogView;
	
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
	public View getChildView(final int teamPos, int playerPos, boolean isLastChild, View convertView, ViewGroup parent) {

		final Team team = (Team) getGroup(teamPos);
		final Player player = (Player) getChild(teamPos, playerPos);
		ChildViewHolder childViewHolder;
		
		if (convertView == null) {
        	childViewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.team_player, null);
            childViewHolder.state = (TextView)convertView.findViewById(R.id.player_state);
            childViewHolder.name = (TextView)convertView.findViewById(R.id.player_name);
            childViewHolder.picture = (ImageView)convertView.findViewById(R.id.player_in_team_pic);
            childViewHolder.delete_bt = (ImageButton)convertView.findViewById(R.id.delete_player_bt);
            convertView.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) convertView.getTag();	
		}
		
		// Finding out if the player is ready or not
		int state = Color.GRAY;
		if(player.getReady() == true) state = context.getResources().getColor(R.color.middle_blue);
		
		childViewHolder.state.setBackgroundColor(state);
		childViewHolder.state.setTextColor(state);
		childViewHolder.name.setText(player.getName());
		childViewHolder.picture.setImageBitmap(player.getPicture());
		
		// Delete a player from a team
		childViewHolder.delete_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Show a confirm message
      	   		// Create an alert box
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				MessageAlert msg_a;
				
				if (alertDialogView == null) {
					msg_a = new MessageAlert();
					alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
					msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
					alertDialogView.setTag(msg_a);
				} else {
					msg_a = (MessageAlert) alertDialogView.getTag();
	            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
					adbParent.removeView(alertDialogView);
				}
				
				// Choosing the type of message alert
				msg_a.msg.setText(Html.fromHtml(context.getResources().getString(R.string.delete_player_from_team_confirm, player.getName(), team.getName())));
								
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Are you sure ?");
				adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Go back to the screen
		          } });
				adb.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
						// Get concerned Team with Parse
						ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
						teamQuery.whereEqualTo("name", team.getName());
						teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject teamParseObject, ParseException e) {
								if (teamParseObject == null) {
									// Display error message
								} else {
									// Get selected User with Parse
									ParseQuery<ParseUser> playerQuery = ParseUser.getQuery();
									playerQuery.whereEqualTo("username", player.getName());
									playerQuery.findInBackground(new FindCallback<ParseUser>() {
										public void done(List<ParseUser> usersList, ParseException e) {
											if (e==null){
												ParseUser userParseObject = usersList.get(0);
												if(userParseObject.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
													// Display restriction message
													Toast.makeText(context, "You can't delete yourself from the team.", Toast.LENGTH_SHORT).show();
												}
												else{
													// Remove Parse Relation
													teamParseObject.getRelation("players").remove(userParseObject);
													teamParseObject.saveInBackground();
													// Remove java object
													((Team) getGroup(teamPos)).removePlayer(player);
													// Display success message
													Toast.makeText(context, "You just deleted "+userParseObject.getUsername()+" from the team.", Toast.LENGTH_SHORT).show();
												}
											}
											else {
												Toast.makeText(context, "The user can't be deleted.", Toast.LENGTH_SHORT).show();
											}
										}
									});
								}
							}
						});

		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
			
			}
		});
		
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

		final Team team = (Team) getGroup(teamPos);
		final int teamPosition = teamPos;
		GroupViewHolder gholder;
		
		if (convertView == null) {
        	gholder = new GroupViewHolder();
        	convertView = inflater.inflate(R.layout.expandable_row, null);
        	gholder.state = (TextView)convertView.findViewById(R.id.state);
			gholder.name = (TextView)convertView.findViewById(R.id.name);
			gholder.creator = (TextView)convertView.findViewById(R.id.creator);
			gholder.selected = (CheckBox)convertView.findViewById(R.id.select_team);
			gholder.delete_bt = (ImageButton)convertView.findViewById(R.id.delete_bt);
			gholder.add_bt = (ImageButton)convertView.findViewById(R.id.add_bt);
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
		
		// Delete a team
		gholder.delete_bt.setFocusable(false);
		gholder.delete_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				// Show a confirm message
      	   		// Create an alert box
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				MessageAlert msg_a;
				
				if (alertDialogView == null) {
					msg_a = new MessageAlert();
					alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
					msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
					alertDialogView.setTag(msg_a);
				} else {
					msg_a = (MessageAlert) alertDialogView.getTag();
	            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
					adbParent.removeView(alertDialogView);
				}
				
				// Choosing the type of message alert
				msg_a.msg.setText(Html.fromHtml(context.getResources().getString(R.string.delete_team_confirm, team.getName())));
								
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Are you sure ?");
				adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Go back to the screen
		          } });
				adb.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Get concerned team with Parse
						ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
						teamQuery.whereEqualTo("name", team.getName());
						teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject teamParseObject, ParseException e) {
								if (e==null){
									// Remove Team in Parse
									teamParseObject.deleteInBackground();
									// Remove java object
									teams.remove(teamPosition);
									// Display success message
									Toast.makeText(context, "You just deleted the team "+teamParseObject.getString("name")+".", Toast.LENGTH_SHORT).show();
								}
								else {
									Toast.makeText(context, "The team can't be deleted.", Toast.LENGTH_SHORT).show();
								}
							}
						});
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
			}
		});
		
		// Add a player
		gholder.add_bt.setFocusable(false);
		gholder.add_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Display alertDialog to select player name
			    final EditText input = new EditText(context);
			    input.setHint("Player name");
			    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
			    alert.setTitle("Enter a player's name");
			    alert.setView(input);
			    alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	// Get input value
			            final String playerName = input.getText().toString().trim();
			            
			            // Get matching user with Parse
			            ParseQuery<ParseUser> playerQuery = ParseUser.getQuery();
			            playerQuery.whereEqualTo("username", playerName);
			            playerQuery.getFirstInBackground(new GetCallback<ParseUser>() {
			            	public void done(final ParseUser userParseObject, ParseException e) {
			            		if (e==null){
									if (userParseObject == null){
										// If no matching user is found
										Toast.makeText(context, "Sorry, this player doesn't exist.", Toast.LENGTH_SHORT).show();
									} else {
										// Get concerned team with Parse
										ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
										teamQuery.whereEqualTo("name", team.getName());
										teamQuery.whereNotEqualTo("players", userParseObject);
										teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
											public void done(final ParseObject teamParseObject, ParseException e) {
												if (e==null){
													// Add user to the team in Parse
													teamParseObject.getRelation("players").add(userParseObject);
													teamParseObject.saveInBackground();
													// Create java Player
													
													ParseFile avatarFile = userParseObject.getParseFile("avatar");
													
													try {
														byte[] avatarByteArray = avatarFile.getData();
														Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
														avatarBitmap = Bitmap.createScaledBitmap(avatarBitmap, 110, 110, false);
														((Team) getGroup(teamPosition)).addPlayer(new Player(userParseObject.getUsername(), avatarBitmap));	
													} catch (ParseException e1) {
														e1.printStackTrace();
													}
													
													// Display success message
													Toast.makeText(context, "You just added "+userParseObject.getUsername()+" to the team "+teamParseObject.getString("name")+".", Toast.LENGTH_LONG).show();
												}
												else {
													if(e.getCode() == 101) Toast.makeText(context, userParseObject.getUsername()+" is already in the team.", Toast.LENGTH_SHORT).show();
													else Toast.makeText(context, userParseObject.getUsername()+" can't be added to the team. ", Toast.LENGTH_SHORT).show();
												}
												
											}
										});
									}
								}
								else {
									Toast.makeText(context, "The player can't be added to the team.", Toast.LENGTH_SHORT).show();
								}}
			            		
						});
			        }
			    });

			    alert.setNegativeButton("Cancel",
			            new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int whichButton) {
			                    dialog.cancel();
			                }
			            });
			    alert.show();
			}
		});
		
		// Choose a team to start a game
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
		public ImageButton delete_bt;
		public ImageButton add_bt;
	}

	class ChildViewHolder {
		public TextView state;
		public TextView name;
		public ImageView picture;
		public ImageButton delete_bt;
	}

}
