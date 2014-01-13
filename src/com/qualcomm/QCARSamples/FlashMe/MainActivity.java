package com.qualcomm.QCARSamples.FlashMe;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends Activity {

	private final Context context = this;
	private View alertDialogView;
	private PopupWindow popup;
	private ImageButton aboutButton = null;
	private ImageButton closeButton = null;
	private int LOGIN = 2000;

	// Data to get
	private final String EXTRA_LOGIN = "user_login";
	private final String EXTRA_PASSWORD = "user_password";
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final LayoutInflater inflater = LayoutInflater.from(context);		
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		final Button logInButton = (Button) findViewById(R.id.login);
     	
		aboutButton = (ImageButton) findViewById(R.id.bobble_help);
		aboutButton.setOnClickListener(new OnClickListener() {
			@Override 
			public void onClick(View v) { 
				initiatePopupWindow(aboutButton); 
			} 
		});
		
		 // Initialize Parse
	 	Parse.initialize(this, "ysJVmuI4oJDEsyF7YOcQG12WVkLzwQlLrqzt15Fg", "YTTLp7GRoHYEMzLXa58T2zB7mcTTPWJuB19JcGnJ");
	 	ParseAnalytics.trackAppOpened(getIntent());
	 	
	 	logInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Testing the data entered in the fields
				final String s_username = username.getText().toString();
				final String s_password = password.getText().toString();
                
				ParseUser.logInInBackground(s_username, s_password, new LogInCallback() {
					public void done(ParseUser user, ParseException e) {
						if (user != null) {
							
							// The user is logged in.
							Intent intent = new Intent(MainActivity.this, ContentActivity.class);
							intent.putExtra(EXTRA_LOGIN, s_username);
							intent.putExtra(EXTRA_PASSWORD, s_password);
							startActivityForResult(intent, LOGIN);
							
						} else {
														
							// Login failed, display alert box
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
						}
					}
				});
				
				// If one field is empty
				/*if(s_username.equals("") || s_password.equals("")){
					Toast.makeText(MainActivity.this, R.string.login_or_pass_empty, Toast.LENGTH_SHORT).show();
					return;
				}*/
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
	
	private void initiatePopupWindow(View parentView) { 
		try { 
			// Get the instance of the LayoutInflater 
			LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			View layout = inflater.inflate(R.layout.popup,(ViewGroup) findViewById(R.id.popup)); 
			popup = new PopupWindow(layout, 600, 300, true); 
			popup.showAtLocation(parentView, Gravity.TOP|Gravity.LEFT, locateView(aboutButton).left, locateView(aboutButton).bottom+10);
		
			closeButton = (ImageButton) layout.findViewById(R.id.close_popup); 
			closeButton.setOnClickListener(new OnClickListener() { 
				public void onClick(View v) { 
					popup.dismiss();
				} 
			});
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
	
	public static Rect locateView(View v)
	{
	    int[] loc_int = new int[2];
	    if (v == null) return null;
	    try {
	        v.getLocationOnScreen(loc_int);
	    } catch (NullPointerException npe) {
	        //Happens when the view doesn't exist on screen anymore.
	        return null;
	    }
	    Rect location = new Rect();
	    location.left = loc_int[0];
	    location.top = loc_int[1];
	    location.right = location.left + v.getWidth();
	    location.bottom = location.top + v.getHeight();
	    return location;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);

	    if((requestCode == LOGIN)||(requestCode == RESULT_CANCELED)) {
	    	Intent i = getIntent();
	    	finish();
	    	startActivity(i);
	    }
	    
	}
	
}