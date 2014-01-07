package com.qualcomm.QCARSamples.FlashMe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.qualcomm.QCARSamples.FlashMe.MessageAlert;

public class SignUpActivity extends Activity {

	private Context context;
	private ParseUser newUser;
	private View alertDialogView;
    private static final int CAMERA_REQUEST = 1888; 
    private ImageView avatarView;
    private ParseFile avatarParseFile;
    private final int PICK_IMAGE = 1000;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        context = SignUpActivity.this;
        final LayoutInflater inflater = LayoutInflater.from(context);
        
        // Get activity elements
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		final EditText email = (EditText) findViewById(R.id.mail);
        this.avatarView = (ImageView)this.findViewById(R.id.pic_empty);
        
        // filling the database with avatarParseFile
        Drawable avatarDrawable = avatarView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)avatarDrawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        this.avatarParseFile = new ParseFile("avatar.png", bitmapdata);
        avatarParseFile.saveInBackground(new SaveCallback() {
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
      	   		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
      	   		Matcher m = p.matcher(s_email);
      	   		if (!m.matches()) {
      	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
      	   			return;
      	   		}
				
      	   		// If nothing is wrong, add new User to DataBase
             	newUser = new ParseUser();
             	newUser.setUsername(s_username);
             	newUser.setPassword(s_password);
             	newUser.setEmail(s_email);
             	newUser.put("avatar", avatarParseFile);
             	newUser.signUpInBackground(new SignUpCallback() {
             		public void done(ParseException e) {
             			if (e == null) {
             				// Notify the user his account has been created and that his marker will be sent by mail
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
            				msg_a.msg.setText(context.getResources().getString(R.string.account_created));
            				
            				
            				// Filling the alert box
            				adb.setView(alertDialogView);
            				adb.setTitle("Success !");
            				adb.setPositiveButton("VIEW PROFILE", new DialogInterface.OnClickListener() {
            		            public void onClick(DialogInterface dialog, int which) {
            		            	Intent intent = new Intent(context, ContentActivity.class);
            		            	startActivity(intent);
            		        } });
            				
            				// Showing the alert box
            		        adb.create();
            				adb.show();
             			} else {
							Toast.makeText(SignUpActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
             			}
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
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
    		switch(requestCode){
    			
    			case CAMERA_REQUEST:
    				
    				// Replacing the preview by the chosen image
    				Bitmap photo = (Bitmap) data.getExtras().get("data"); 
    				avatarView.setImageBitmap(photo);
    				
    				// Replacing the avatar in the database
    				ByteArrayOutputStream stream = new ByteArrayOutputStream();
    				photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
						Bitmap photoPicked = Bitmap.createScaledBitmap(bm, 200, 200, false);
						avatarView.setImageBitmap(photoPicked);
						
						// Replacing the avatar in the database
	    				ByteArrayOutputStream streamPicked = new ByteArrayOutputStream();
	    				photoPicked.compress(Bitmap.CompressFormat.PNG, 100, streamPicked);
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
    		Toast.makeText(SignUpActivity.this, "No Photo Selected", Toast.LENGTH_SHORT).show();
    	}
    }
}
