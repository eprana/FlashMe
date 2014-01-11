package com.qualcomm.QCARSamples.FlashMe;

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
import com.parse.SignUpCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
	
	static ParseUser currentUser = ParseUser.getCurrentUser();
	Context context;
	private ImageView avatarView;
	private Button changePic;
	private EditText updateMail;
	private EditText updatePass;
	private Button saveChanges;
	private ImageButton backButton;
	private String s_password;
	private String s_mail;
	private View alertDialogView;
	private LayoutInflater layoutInflater;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile);
	
		context = EditActivity.this;
		avatarView = (ImageView) findViewById(R.id.profile_picture);
		changePic = (Button) findViewById(R.id.change_pic);
		updateMail = (EditText) findViewById(R.id.new_mail);
		updatePass = (EditText) findViewById(R.id.new_pass);
		saveChanges = (Button) findViewById(R.id.save_changes);
		backButton = (ImageButton) findViewById(R.id.back_bt);
		layoutInflater = getLayoutInflater();
		
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
				
//		changePic.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		saveChanges.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				s_password = updatePass.getText().toString();
				s_mail = updateMail.getText().toString();
				
				// Testing if the fields have been modified
				if(!(s_mail.equals("")) || !(s_password.equals(""))){
					
//					ParseQuery<ParseUser> query = ParseUser.getQuery();
//			    	query.whereEqualTo("username", currentUser.getUsername());
//			    	query.findInBackground(new FindCallback<ParseUser>() {
//			      	public void done(List<ParseUser> objects, ParseException e) {
//			      	
//			      		if (e == null) {
//			      	    	// Modifying the fields
//			      	    	final ParseUser userd = (ParseUser) objects.get(0);
//			      	    	
//			      	    	if(!s_mail.equals("")){
//			    				// Testing if mail is right
//			    				Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
//			          	   		Matcher m = p.matcher(s_mail);
//			          	   		if (!m.matches()) {
//			          	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
//			          	   			return;
//			          	   		}else{
//				          	   		userd.signUpInBackground(new SignUpCallback() {
//	
//										@Override
//										public void done(ParseException arg0) {
//											userd.setEmail(s_mail);										
//										}
//				          	   		});
//			          	   		}
//			      	    	}		
//			      	    	if(!s_password.equals("")){
//			      	    		userd.signUpInBackground(new SignUpCallback() {
//			      	    			
//									@Override
//									public void done(ParseException arg0) {
//										userd.setPassword(s_password);										
//									}
//			          	   		});
//			      	    	}
//			      	    	
//				      	    } else{
//				      	    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
//				      	    } 
//			      		}
//			      	});
				}
				//Sending alert & Changing page
      	   		// Create an alert box
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				MessageAlert msg_a;
				
				if (alertDialogView == null) {
					msg_a = new MessageAlert();
					alertDialogView = layoutInflater.inflate(R.layout.alert_dialog, null);
					msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
					alertDialogView.setTag(msg_a);
				} else {
					msg_a = (MessageAlert) alertDialogView.getTag();
	            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
					adbParent.removeView(alertDialogView);
				}
				
				// Choosing the type of message alert
				msg_a.msg.setText(context.getResources().getString(R.string.changes_saved));				
				
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Success !");
				adb.setPositiveButton("BACK TO PROFILE", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	finish();
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
				
			}
		});
		
		backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}	
}
