package com.qualcomm.QCARSamples.FlashMe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.qualcomm.QCARSamples.FlashMe.MessageAlert;

public class SignUpActivity extends Activity {

	private Context context;
	private View alertDialogView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        context = SignUpActivity.this;
        
        // Get activity elements
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		final EditText mail = (EditText) findViewById(R.id.mail);
        
        final LayoutInflater inflater = LayoutInflater.from(context);

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
        		final String s_mail = mail.getText().toString();

				// If one field is empty
				if(s_username.equals("") || s_password.equals("")){
					Toast.makeText(context, R.string.login_or_pass_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// If the e-mail is invalid
      	        // Declaring pattern and matcher we need to compare
      	   		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
      	   		Matcher m = p.matcher(s_mail);
      	   		if (!m.matches()) {
      	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
      	   			return;
      	   		}
				
      	   		// If nothing is wrong, add new User to DataBase
             	ParseObject newUser = new ParseObject("User");
             	newUser.put("username", s_username);
             	newUser.put("password", s_password);
             	newUser.put("mail", s_mail);
             	newUser.saveInBackground();
             	
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