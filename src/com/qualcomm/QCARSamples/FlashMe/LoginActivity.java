package com.qualcomm.QCARSamples.FlashMe;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class LoginActivity extends Activity {

@Override
protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Parse.initialize(this, "ysJVmuI4oJDEsyF7YOcQG12WVkLzwQlLrqzt15Fg", "YTTLp7GRoHYEMzLXa58T2zB7mcTTPWJuB19JcGnJ");
		ParseAnalytics.trackAppOpened(getIntent());
    	
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("user", "yann");
		testObject.saveInBackground();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}