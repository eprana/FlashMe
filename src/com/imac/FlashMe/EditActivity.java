package com.imac.FlashMe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.imac.FlashMe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {
	
	static ParseUser currentUser;
	Context context;
	private ImageView avatarView;
	private EditText updateMail;
	private EditText updatePass;
	private Button saveChanges;
	private Button deleteProfile;
	private String s_password;
	private String s_mail;
	private View alertDialogView;
	private LayoutInflater layoutInflater;
	private static final int CAMERA_REQUEST = 1888; 
	private final int PICK_IMAGE = 1000;
    private ParseFile avatarParseFile;
    int width = 300;
	int height = 300;
	int scaleX;
	int scaleY;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		context = EditActivity.this;
		
		getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
		
		currentUser = ParseUser.getCurrentUser();
		avatarView = (ImageView) findViewById(R.id.profile_picture);
		updateMail = (EditText) findViewById(R.id.new_mail);
		updatePass = (EditText) findViewById(R.id.new_pass);
		saveChanges = (Button) findViewById(R.id.save_changes);
		deleteProfile = (Button) findViewById(R.id.delete_profile);
		layoutInflater = getLayoutInflater();
		avatarParseFile = null;
		
		// Setting profile picture
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", currentUser.getUsername());
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			public void done(ParseUser user, ParseException e) {
			    if (e == null) {
			    	ParseFile avatarFile = (ParseFile) user.get("avatar");
			    	try {
						byte[] avatarByteArray = avatarFile.getData();
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
						// Setting the imageView
						avatarView.setImageBitmap(avatarBitmap);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
			    } else{
		  	    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		  	    }
			  }
			});
		
		
		// Choose picture button
        Button folderButton = (Button) this.findViewById(R.id.change_pic);
        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent();
            	intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        
        // Take picture button
        Button photoButton = (Button) this.findViewById(R.id.take_new_pic);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                startActivityForResult(cameraIntent, CAMERA_REQUEST); 
            }
        });
		
        // Delete button
        deleteProfile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
				alertDialog.setTitle("Delete profile");
				alertDialog.setMessage("Are you sure you want to delete your profile ?");
				alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User wants to delete profile
						deleteProfile();
					}
				});
				alertDialog.setNegativeButton("CANCEL", null);
				alertDialog.create();
				alertDialog.show();	
			}
		});
        
		// Saving button
		saveChanges.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				s_password = updatePass.getText().toString();
				s_mail = updateMail.getText().toString();
				
				// Testing if the fields have been modified
				if(!(s_mail.equals("")) || !(s_password.equals(""))|| (avatarParseFile != null)){
					
					ParseQuery<ParseUser> query = ParseUser.getQuery();
			    	query.whereEqualTo("username", currentUser.getUsername());
			    	query.getFirstInBackground(new GetCallback<ParseUser>() {

						@Override
						public void done(ParseUser user, ParseException e) {
				      		
							if (e != null) {
					      	   	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							}
							
							// Update the fields  
							
							// If the mail has changed
			      	    	if(!s_mail.equals("")){
			    				// Testing if mail is right
			    				Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
			          	   		Matcher m = p.matcher(s_mail);
			          	   		if (!m.matches()) {
			          	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
			          	   			return;
			          	   		}else{
				          	   		user.put("email", s_mail);
				          	   		user.saveInBackground();
			          	   		}
			      	    	}	
			      	    	
			      	    	// If the password has changed
			      	    	if(!s_password.equals("")){
								user.put("password", s_password);
								user.saveInBackground();
			      	    	}
			      	    	
			      	    	// If the picture has changed
							if(avatarParseFile != null){
								final ProgressDialog waitingDialog = ProgressDialog.show(context, "Uploading picture", "Waiting for you new picture to upload...", true);
								waitingDialog.show();
								user.put("avatar", avatarParseFile);
								user.saveInBackground(new SaveCallback() {
									
									@Override
									public void done(ParseException e) {
										waitingDialog.dismiss();
										displaySuccessDialog();
									}
								});
							}
							else {
								displaySuccessDialog();
							}
				      	}
	
			      	});
				}
			}
		});
	}
	
	private void displaySuccessDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle("Success");
		alertDialog.setMessage("Your changes have been saved successfully.");
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
				Intent intent = new Intent(getApplicationContext(), ContentActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;
		    	startActivity(intent);
			}
		});
		alertDialog.create();
		alertDialog.show();	
	}
	
	private void deleteProfile() {
		ParseQuery<ParseObject> teamsQuery = new ParseQuery<ParseObject>("Team");
		teamsQuery.whereEqualTo("createdBy", currentUser);
		teamsQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> teams, ParseException e) {
				for(ParseObject team : teams) {
					team.deleteInBackground();
				}
			}
		});
		ParseQuery<ParseObject> gamesQuery = new ParseQuery<ParseObject>("Game");
		gamesQuery.whereEqualTo("createdBy", currentUser);
		gamesQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> games, ParseException e) {
				for(ParseObject game : games) {
					game.deleteInBackground();
				}
			}
		});
		currentUser.deleteInBackground();
		ParseUser.logOut();
		finish();
		Intent intent = new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
    		switch(requestCode){
    			
    			case CAMERA_REQUEST:
    				
    				// Replacing the preview by the chosen image
    				//Bitmap avatar = Bitmap.createScaledBitmap((Bitmap) data.getExtras().get("data"), 300, 300, false);
    				Bitmap avatar = (Bitmap) data.getExtras().get("data");
    				avatar = Bitmap.createScaledBitmap(avatar, width, height, false);
    				avatarView.setImageBitmap(avatar);
    				
    				// Replacing the avatar in the database
    				ByteArrayOutputStream stream = new ByteArrayOutputStream();
    				avatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
    				byte[] avatarByteArray = stream.toByteArray();
    				
    				avatarParseFile = new ParseFile("avatar.png", avatarByteArray);    				
                 	avatarParseFile.saveInBackground(new SaveCallback() {
    					public void done(ParseException e) {
    						if (e != null) {
    							Toast.makeText(context, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
    						}
    					}
    				});    						    				
    	            break;
    	            
    			case PICK_IMAGE:
    				
    				Uri selectedImage = data.getData();
    	            if (selectedImage != null) {
    	            	Bitmap bm = null;
						try {
							bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						// Replacing the preview image by the chosen image
	    				//Bitmap avatarPicked = Bitmap.createScaledBitmap(bm, 300, 300, false);
						Bitmap avatarPicked = Bitmap.createScaledBitmap(bm, width, height, false);
						avatarView.setImageBitmap(avatarPicked);
						
						// Replacing the avatar in the database
	    				ByteArrayOutputStream streamPicked = new ByteArrayOutputStream();
	    				
	    				avatarPicked.compress(Bitmap.CompressFormat.PNG, 100, streamPicked);
	    				byte[] avatarByteArrayPicked = streamPicked.toByteArray();
	    				
	    				avatarParseFile = new ParseFile("avatar.png", avatarByteArrayPicked);
	                 	avatarParseFile.saveInBackground(new SaveCallback() {
	    					public void done(ParseException e) {
	    						if (e != null) {
	    							Toast.makeText(context, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
	    						}
	    					}
	    				});
    	            } else {
    	                Toast.makeText(EditActivity.this, "Error getting Image",Toast.LENGTH_SHORT).show();
    	            }

    	            break;
    	        
    			default:
    	        	break;
    				
    		}
    	}else if(resultCode == Activity.RESULT_CANCELED) {
    		Toast.makeText(EditActivity.this, "No Photo Selected", Toast.LENGTH_SHORT).show();
    	}
    }
}
