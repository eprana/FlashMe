package com.qualcomm.QCARSamples.FlashMe;

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
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.qualcomm.QCARSamples.FlashMe.MessageAlert;

public class SignUpActivity extends Activity {

	private Context context;
	private View alertDialogView;
    private static final int CAMERA_REQUEST = 1888; 
    private ImageView imageView;
    private final int PICK_IMAGE = 1000;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        context = SignUpActivity.this;
        
        // Get activity elements
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		final EditText email = (EditText) findViewById(R.id.mail);
        
        final LayoutInflater inflater = LayoutInflater.from(context);
        this.imageView = (ImageView)this.findViewById(R.id.pic_empty);
        
        Button folderButton = (Button) this.findViewById(R.id.choose_pic);
        folderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//            	Intent cameraIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(cameraIntent, PICK_IMAGE);
            	Intent intent = new Intent();
            	intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        
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
     	
        // Initialize Parse
     	Parse.initialize(this, "ysJVmuI4oJDEsyF7YOcQG12WVkLzwQlLrqzt15Fg", "YTTLp7GRoHYEMzLXa58T2zB7mcTTPWJuB19JcGnJ");
     	ParseAnalytics.trackAppOpened(getIntent());
     	
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
             	ParseUser user = new ParseUser();
             	user.setUsername(s_username);
             	user.setPassword(s_password);
             	user.setEmail(s_email);
             	user.signUpInBackground(new SignUpCallback() {
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
             				// Sign up didn't succeed. Look at the ParseException
             				// to figure out what went wrong
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
    				Bitmap photo = (Bitmap) data.getExtras().get("data"); 
    	            imageView.setImageBitmap(photo);
    	            break;
    	            
    			case PICK_IMAGE:
    				Uri selectedImage = data.getData();
    	            if (selectedImage != null) {
    	            	Bitmap bm = null;
						try {
							bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	            	imageView.setImageBitmap(Bitmap.createScaledBitmap(bm, 200, 200, false));
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
    	
//        if (resultCode == Activity.RESULT_OK) {
//                    } else if (resultCode == Activity.RESULT_CANCELED) {
//            Toast.makeText(PhotoTake.this, "No Photo Selected",
//                    Toast.LENGTH_SHORT).show();
//        }
//        
//        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
//        	Bitmap photo = (Bitmap) data.getExtras().get("data"); 
//            imageView.setImageBitmap(photo);
//        }
//        
//        
//        if (requestCode == SELECT_PICTURE) {
//            Uri selectedImageUri = data.getData();
//            selectedImagePath = getPath(selectedImageUri);
//        }
//        
        
        
    } 
}