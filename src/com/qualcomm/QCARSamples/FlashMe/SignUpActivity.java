package com.qualcomm.QCARSamples.FlashMe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {

	private Context context;
	private View alertDialogView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        
        context = SignUpActivity.this;
        final LayoutInflater inflater = LayoutInflater.from(context);
        
        final EditText login = (EditText) findViewById(R.id.username);
        final EditText mail = (EditText) findViewById(R.id.mail);
        final EditText pass = (EditText) findViewById(R.id.password);
		
        final ImageButton backButton = (ImageButton) findViewById(R.id.back_bt);
        final Button signUpButton = (Button) findViewById(R.id.signup);
        
        // When the SIGN UP button is pressed 
        signUpButton.setOnClickListener(new OnClickListener() {
  			
      		@Override
      		public void onClick(View v) {
      			
      			// Testing the data entered in the fields
				final String loginTxt = login.getText().toString();
				final String mailTxt = mail.getText().toString();
				final String passTxt = pass.getText().toString();
				
				// If one field is empty
				if(loginTxt.equals("") || passTxt.equals("")){
					Toast.makeText(context, R.string.login_or_pass_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// If the e-mail is invalid
      	        // Declaring pattern and matcher we need to compare
      	   		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
      	   		Matcher m = p.matcher(mailTxt);
      	   		if (!m.matches()) {
      	   			Toast.makeText(context, R.string.wrong_mail_pattern, Toast.LENGTH_SHORT).show();
      	   			return;
      	   		}
      	   		
      	   // If nothing is wrong, going to the profile page and sending data
      	   		
      	   	
      	   		
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
		            	Intent intent = new Intent(context, ProfileActivity.class);
		            	startActivity(intent);
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
      	   		
				//intent.putExtra(EXTRA_LOGIN,  login.getText().toString());
				//intent.putExtra(EXTRA_PASSWORD,  pass.getText().toString());
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