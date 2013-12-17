package com.qualcomm.QCARSamples.FlashMe;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity {

	private final Context context = this;
	private View alertDialogView;

	// Data to get
	private final String EXTRA_LOGIN = "user_login";
	private final String EXTRA_PASSWORD = "user_password";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final LayoutInflater inflater = LayoutInflater.from(context);
		
		// ------------> User tries to log in
		
		final EditText login = (EditText) findViewById(R.id.username);
		final EditText pass = (EditText) findViewById(R.id.password);
		final Button logInButton = (Button) findViewById(R.id.login);
     	
		logInButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// Testing the data entered in the fields
				final String loginTxt = login.getText().toString();
				final String passTxt = pass.getText().toString();
                
				// If one field is empty
				if(loginTxt.equals("") || passTxt.equals("")){
					Toast.makeText(MainActivity.this, R.string.login_or_pass_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// If one field is unknown
				if(loginTxt.equals("Dam") || passTxt.equals("anus")){

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
					}

					// Choosing the type of message alert
					msg_a.msg.setText(context.getResources().getString(R.string.wrong_username_or_pass));
					
					// Filling the alert box
					adb.setView(alertDialogView);
					adb.setTitle("Ooops !");
					adb.setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	// Going back to the front screen and deleting the alertDialogView
			            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
							adbParent.removeView(alertDialogView);
			          } });
					adb.setPositiveButton("SIGN UP", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
							startActivity(intent);
			          } });
					
					// Showing the alert box
			        adb.create();
					adb.show();
					
					return;
				}
				
				// If nothing is wrong, going to the profile page and sending data
				Intent intent = new Intent(MainActivity.this, ContentActivity.class);
				intent.putExtra(EXTRA_LOGIN,  login.getText().toString());
				intent.putExtra(EXTRA_PASSWORD,  pass.getText().toString());
				startActivity(intent);
			}
		});
		
		// ------------> Sign up
		
		final Button signUpButton = (Button) findViewById(R.id.signup);
		signUpButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
				startActivity(intent);
			}
		});
	}		
}