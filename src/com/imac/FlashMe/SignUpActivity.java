package com.imac.FlashMe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imac.FlashMe.MessageAlert;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.imac.FlashMe.R;

public class SignUpActivity extends Activity {

	private Context context;
	private LayoutInflater inflater;
	private ParseUser newUser;
	private View alertDialogView;
    private static final int CAMERA_REQUEST = 1888; 
    private ImageView avatarView;
    private ParseFile avatarParseFile;
    private ParseFile markerParseFile;
    private final int PICK_IMAGE = 1000;
    private final int CREATE_PROFILE = 1404;
    private boolean hasChanged = false;
    private Bitmap bitmapToSend;
    private Bitmap bitmapMarkerToSend;
    private boolean hasBeenCreated = false;
    private DisplayMetrics screen = new DisplayMetrics();
    int pictureSize = 0;
    
    private EditText username;
    private EditText password;
    private EditText email;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        context = SignUpActivity.this;
        inflater = LayoutInflater.from(context);
        
        getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
        
        getWindowManager().getDefaultDisplay().getMetrics(screen);
        pictureSize = screen.widthPixels/3;
        
        // Get activity elements
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		email = (EditText) findViewById(R.id.mail);
        avatarView = (ImageView)this.findViewById(R.id.pic_empty);
        
        if(!hasChanged){
        	bitmapToSend = ((BitmapDrawable)getResources().getDrawable(R.drawable.default_profile_picture_thumb)).getBitmap();
        }
        
        // Fill the database with avatarParseFile
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapToSend.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //bitmapToSend = Bitmap.createScaledBitmap(bitmapToSend, bitmapToSend.getWidth(), bitmapToSend.getHeight(), false);
        byte[] bitmapdata = stream.toByteArray();
        avatarParseFile = new ParseFile("avatar.png", bitmapdata);
        avatarParseFile.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(context, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}); 
        
        // Send an empty marker to the database
        bitmapMarkerToSend = ((BitmapDrawable)getResources().getDrawable(R.drawable.default_team_picture_thumb)).getBitmap();
        ByteArrayOutputStream markerStream = new ByteArrayOutputStream();
        bitmapMarkerToSend = Bitmap.createScaledBitmap(bitmapMarkerToSend, bitmapMarkerToSend.getWidth(), bitmapMarkerToSend.getHeight(), false);
        //bitmapMarkerToSend.compress(Bitmap.CompressFormat.PNG, 100, markerStream);
        byte[] bitmapMarkerData = markerStream.toByteArray();
        this.markerParseFile = new ParseFile("marker.png", bitmapMarkerData);
        markerParseFile.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(context, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});        
        
        // Choose picture button
        Button folderButton = (Button) this.findViewById(R.id.choose_pic);
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
        Button photoButton = (Button) this.findViewById(R.id.take_pic);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                startActivityForResult(cameraIntent, CAMERA_REQUEST); 
            }
        });

        final ImageButton backButton = (ImageButton) findViewById(R.id.back_bt);
        final Button signUpButton = (Button) findViewById(R.id.signup);
     	
        // Click on sign up button
        signUpButton.setOnClickListener(new View.OnClickListener() {

        	@Override
        	public void onClick(View v) {
        		// Get input values
        		final String s_username = username.getText().toString();
        		final String s_password = password.getText().toString();
        		final String s_email = email.getText().toString();

				// If one field is empty
				if(s_username.equals("") || s_password.equals("")){
					Toast.makeText(context, R.string.login_or_pass_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// If the e-mail is invalid
      	        // Declaring pattern and matcher we need to compare
				String regExpn =
			             "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
			                 +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			                   +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
			                   +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			                   +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
			                   +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";


				Pattern p = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
      	   		Matcher m = p.matcher(s_email);
      	   		if (!m.matches()) {
      	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
      	   			return;
      	   		}
      	   		
      	   		// If nothing is wrong, add new User to DataBase and sand e-mail
             	newUser = new ParseUser();
             	newUser.setUsername(s_username);
             	newUser.setPassword(s_password);
             	newUser.setEmail(s_email);
             	newUser.put("state", 0);
             	newUser.put("avatar", avatarParseFile);
             	//newUser.put("marker", markerParseFile);
             	newUser.put("totalScore", 0);
             	newUser.put("bestScore", 0);
             	newUser.put("rank", 0);
             	newUser.put("victories", 0);
             	newUser.put("defeats", 0);
             	newUser.signUpInBackground(new SignUpCallback() {
             		public void done(ParseException e) {
             			if (e != null) {
							Toast.makeText(SignUpActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
             			}
         				// Assign markerId to user
             	    	ParseQuery<ParseObject> nextMarkerId = ParseQuery.getQuery("NextMarkerId");
             	    	nextMarkerId.getFirstInBackground(new GetCallback<ParseObject>() {
             				
             				@Override
             				public void done(final ParseObject markerId, ParseException e) {
             					final int id =  markerId.getInt("value");
             					newUser.put("markerId", id);
             					newUser.saveInBackground(new SaveCallback() {
									
									@Override
									public void done(ParseException e) {
										if (e != null) {
											Toast.makeText(context, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
											return;
										}
										markerId.put("value", id+1);
										markerId.saveInBackground();
				             			sendConfirmationEmail(s_username, s_email, s_password);
				             			showSuccessAlertBox();
									}
								});
             				}
             			});
             		}
             	});
        	}
        });  
   			
   		backButton.setOnClickListener(new OnClickListener() {
	      			
      		@Override
      		public void onClick(View v) {
      			finish();
      		}
      	});      
    }
    
    private void sendConfirmationEmail(String username, String email, String password) {
		SendMailToUser mail = new SendMailToUser(context);
	    String subject = "Welcome !";
	    String message = context.getResources().getString(R.string.email_to_send, username, password, "http://www.pouet.fr");
	    mail.sendMail(email, subject, message);
    }
    
    private void showSuccessAlertBox() {

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
		msg_a.msg.setText(context.getResources().getString(R.string.account_created));
		
		// Filling the alert box
		adb.setView(alertDialogView);
		adb.setTitle("Success !");
		adb.setPositiveButton("VIEW PROFILE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	hasBeenCreated = true;
            	Intent intent = new Intent(context, ContentActivity.class);
            	startActivityForResult(intent, CREATE_PROFILE);
        } });
		
		// Showing the alert box
        adb.create();
		adb.show();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
    		switch(requestCode){
    			
    			case CAMERA_REQUEST:
    				
    				hasChanged = true;
    				
    				// Replacing the preview by the chosen image
    				//Bitmap avatar = Bitmap.createScaledBitmap((Bitmap) data.getExtras().get("data"), pictureSize, pictureSize, false);
    				Bitmap avatar = (Bitmap) data.getExtras().get("data");
    				avatarView.setImageBitmap(avatar);
    				
    				bitmapToSend = avatar;
    				
    				// Replacing the avatar in the database
    				ByteArrayOutputStream stream = new ByteArrayOutputStream();
    				bitmapToSend.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
    				
    				hasChanged = true;
    				
    				Uri selectedImage = data.getData();
    	            if (selectedImage != null) {
    	            	Bitmap bm = null;
						try {
							bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						// Replacing the preview image by the chosen image
						
	    				//Bitmap avatarPicked = Bitmap.createScaledBitmap(bm, pictureSize, pictureSize, false);
						
						avatarView.setImageBitmap(bm);
						bitmapToSend = bm;
						
						// Replacing the avatar in the database
	    				ByteArrayOutputStream streamPicked = new ByteArrayOutputStream();
	    				bitmapToSend.compress(Bitmap.CompressFormat.PNG, 100, streamPicked);
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
    	                Toast.makeText(SignUpActivity.this, "Error getting Image",Toast.LENGTH_SHORT).show();
    	            }

    	            break;
    	        
    			default:
    	        	break;
    				
    		}
    	}else if(resultCode == Activity.RESULT_CANCELED) {
    		if(hasBeenCreated){
    			finish();
    		}else Toast.makeText(SignUpActivity.this, "No Photo Selected", Toast.LENGTH_SHORT).show();
    	}
    }
}
