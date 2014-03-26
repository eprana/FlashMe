package com.imac.FlashMe;

import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.imac.FlashMe.R;
import com.imac.VuforiaApp.SampleApplicationSession;

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
		
		getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
		
		 // Initialize Parse
	 	Parse.initialize(this, "ysJVmuI4oJDEsyF7YOcQG12WVkLzwQlLrqzt15Fg", "YTTLp7GRoHYEMzLXa58T2zB7mcTTPWJuB19JcGnJ");
	 	ParseAnalytics.trackAppOpened(getIntent());

	 	
//	 	ParseUser.logInInBackground("Xopi", "xopi", new LogInCallback() {
//			public void done(ParseUser user, ParseException e) {
//				Intent intent = new Intent(MainActivity.this, ContentActivity.class);
//				intent.putExtra(EXTRA_LOGIN, "Xopi");
//				intent.putExtra(EXTRA_PASSWORD, "xopi");
//				startActivityForResult(intent, LOGIN);
//			}
//	 	});
	 	
	 	if (!(ParseUser.getCurrentUser() == null)) {
	 		//ParseUser.getCurrentUser().put("state", 0);
			ParseUser.logOut();
	 	}
	 	
	 	logInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
					AlertDialog.Builder noInternetDialog = new AlertDialog.Builder(context);
					noInternetDialog.setTitle("Ooops");
					noInternetDialog.setMessage("You need internet access to use Flash Me...");
					noInternetDialog.setPositiveButton("OK", null);
					noInternetDialog.create();
					noInternetDialog.show();
					return;
				}
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
							AlertDialog.Builder loginFailed = new AlertDialog.Builder(context);

							// Filling the alert box
							loginFailed.setView(alertDialogView);
							loginFailed.setTitle("Ooops !");
							loginFailed.setMessage("Seems like you haven\'t created an account yet.");
							loginFailed.setNegativeButton("RETRY", null);
							loginFailed.setPositiveButton("SIGN UP", new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int which) {
					            	Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
									startActivity(intent);
					          } });
							
							// Showing the alert box
							loginFailed.create();
							loginFailed.show();
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
			View layout = inflater.inflate(R.layout.about_popup,(ViewGroup) findViewById(R.id.popup)); 
			popup = new PopupWindow(layout, 180, 210, true);
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