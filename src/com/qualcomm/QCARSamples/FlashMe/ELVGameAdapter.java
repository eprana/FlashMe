package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ELVGameAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Game> games;
	private LayoutInflater inflater;
	private Game selectedGame;
	private int lastSelectedPosition;
	private CompoundButton lastSelectedButton;
	private View alertDialogView;
	
	public ELVGameAdapter(Context context, ArrayList<Game> games){
		this.context = context;
		this.games = games;
		this.inflater = LayoutInflater.from(context);
		this.selectedGame = null;
		this.lastSelectedPosition = -1;
		this.lastSelectedButton = null;
	}
	
	@Override
	public boolean areAllItemsEnabled(){
		return true;
	}
	
	@Override
	public Object getChild(int gamePos, int teamPos) {
		return games.get(gamePos).getTeams().get(teamPos);
	}

	@Override
	public long getChildId(int gamePos, int teamPos) {
		return teamPos;
	}

	@Override
	public View getChildView(int gamePos, int teamPos, boolean isLastChild, View convertView, ViewGroup parent) {

		final Game game = (Game) getGroup(gamePos);
		final Team team = (Team) getChild(gamePos, teamPos);
		ChildViewHolder childViewHolder;
		
		if (convertView == null) {
        	childViewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.game_team, null);
            childViewHolder.state = (TextView)convertView.findViewById(R.id.team_state);
            childViewHolder.name = (TextView)convertView.findViewById(R.id.team_name);
            childViewHolder.picture = (ImageView)convertView.findViewById(R.id.team_pic);
            childViewHolder.delete_bt = (ImageButton)convertView.findViewById(R.id.delete_team_bt);
            convertView.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) convertView.getTag();	
		}
		
		// finding out if the player is ready or not
		int state = Color.GRAY;
		if(team.getReady() == true) state = context.getResources().getColor(R.color.middle_blue);
		
		childViewHolder.state.setBackgroundColor(state);
		childViewHolder.state.setTextColor(state);
		childViewHolder.name.setText(team.getName());
		childViewHolder.picture.setImageDrawable(team.getPicture());
		
		// Delete a team from a game
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
				msg_a.msg.setText(Html.fromHtml(context.getResources().getString(R.string.delete_team_from_game_confirm, team.getName(), game.getName())));
								
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Are you sure ?");
				adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Go back to the screen 
		          } });
				adb.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Get concerned Game with Parse
						ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery("Game");
						gameQuery.whereEqualTo("name", game.getName());
						gameQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject gameParseObject, ParseException e) {
								if (gameParseObject == null) {
									// Display error message
								} else {
									// Get selected Team with Parse
									ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
									teamQuery.whereEqualTo("name", team.getName());
									teamQuery.findInBackground(new FindCallback<ParseObject>() {
										public void done(List<ParseObject> teamsList, ParseException e) {
											if (e==null){
												ParseObject teamParseObject = teamsList.get(0);
												// Remove Parse Relation
												gameParseObject.getRelation("teams").remove(teamParseObject);
												gameParseObject.saveInBackground();
												// Remove java object
												game.removeTeam(team);
												// Display success message
												Toast.makeText(context, "You just deleted the team "+teamParseObject.getString("name")+" from the game "+gameParseObject.getString("name")+".", Toast.LENGTH_LONG).show();
											}
											else {
												// Display error message
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
	public int getChildrenCount(int gamePos) {
		return games.get(gamePos).getTeams().size();
	}

	@Override
	public Object getGroup(int gamePos) {
		return games.get(gamePos);
	}

	@Override
	public int getGroupCount() {
		return games.size();
	}

	@Override
	public long getGroupId(int gamePos) {
		return gamePos;
	}

	@Override
	public View getGroupView(int gamePos, boolean isExpanded, View convertView, ViewGroup parent) {

		final int gamePosition = gamePos;
		final Game game = (Game) getGroup(gamePos);
		
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

		// finding out if the team is ready or not
		int state = Color.GRAY;
		if(games.get(gamePos).getReady() == true){
			state = context.getResources().getColor(R.color.middle_blue);
		}
		
		gholder.state.setBackgroundColor(state);
		gholder.state.setTextColor(state);
		gholder.name.setText(games.get(gamePos).getName());
		gholder.creator.setText(games.get(gamePos).getCreator());	
		
		// Delete a game
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
				msg_a.msg.setText(Html.fromHtml(context.getResources().getString(R.string.delete_game_confirm, game.getName())));
								
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
						ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Game");
						teamQuery.whereEqualTo("name", game.getName());
						teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject gameParseObject, ParseException e) {
								if (e==null){
									// Remove Team in Parse
									gameParseObject.deleteInBackground();
									// Remove java object
									games.remove(gamePosition);
									// Display success message
									Toast.makeText(context, "You just deleted the game "+gameParseObject.getString("name")+".", Toast.LENGTH_SHORT).show();
								}
								else {
									Toast.makeText(context, "The game can't be deleted.", Toast.LENGTH_SHORT).show();
								}
								
							}
						});
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
			}
		});
		
		// Add a team
		gholder.add_bt.setFocusable(false);
		gholder.add_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Display alertDialog to select player name
			    final EditText input = new EditText(context);
			    input.setHint("Team name");
			    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
			    alert.setTitle("Enter a team's name");
			    alert.setView(input);
			    alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	// Get input value
			            String teamName = input.getText().toString().trim();
			            
			            // Get matching team with Parse
			            ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Team");
			            teamQuery.whereEqualTo("name", teamName);
			            teamQuery.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> teamsList, ParseException e) {
								if (e==null){
									if (teamsList.isEmpty()){
										// If no matching team is found
										Toast.makeText(context, "Sorry, this team doesn't exist.", Toast.LENGTH_SHORT).show();
									}
									else {
										final ParseObject teamParseObject = teamsList.get(0);
										// Get concerned team with Parse
										ParseQuery<ParseObject> teamQuery = ParseQuery.getQuery("Game");
										teamQuery.whereEqualTo("name", game.getName());
										teamQuery.whereNotEqualTo("teams", teamParseObject);
										teamQuery.getFirstInBackground(new GetCallback<ParseObject>() {
											public void done(final ParseObject gameParseObject, ParseException e) {
												if (e==null){
													
													
													
													// Add team to the game in Parse
													gameParseObject.getRelation("teams").add(teamParseObject);
													gameParseObject.saveInBackground();
													// Create java Player
													try {
														game.addTeam(new Team(teamParseObject.getString("name"), ((ParseUser) teamParseObject.fetch().getParseObject("createdBy")).fetch().getUsername(), context.getResources().getDrawable(R.drawable.default_profile_picture_thumb)));
													} catch (NotFoundException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													} catch (ParseException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
													// Display success message
													Toast.makeText(context, "You just added the team "+teamParseObject.getString("name")+" to the game "+gameParseObject.getString("name")+".", Toast.LENGTH_LONG).show();
												}
												else {
													if(e.getCode() == 101) Toast.makeText(context, teamParseObject.getString("name")+" is already in the game.", Toast.LENGTH_SHORT).show();
													else Toast.makeText(context, teamParseObject.getString("name")+" can't be added to the team. ", Toast.LENGTH_SHORT).show();
												}
												
											}
										});
									}
								}
								else {
									Toast.makeText(context, "The team can't be added to the game.", Toast.LENGTH_SHORT).show();
								}
							}
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
					if(lastSelectedPosition != -1 && gamePosition != lastSelectedPosition) {
						lastSelectedButton.setChecked(false);
					}
					lastSelectedPosition = gamePosition;
					lastSelectedButton = buttonView;
					selectedGame = games.get(gamePosition);
				} else {
					lastSelectedPosition = -1;
					lastSelectedButton = null;
					selectedGame = null;
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
	public boolean isChildSelectable(int gamePos, int teamPos) {
		return false;
	}
	
	public Game getSelectedGame(){
		return selectedGame;
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
