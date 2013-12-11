package com.qualcomm.QCARSamples.FlashMe;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        
        // Get sign up inputs
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
     	
        // Initialize Parse
     	Parse.initialize(this, "ysJVmuI4oJDEsyF7YOcQG12WVkLzwQlLrqzt15Fg", "YTTLp7GRoHYEMzLXa58T2zB7mcTTPWJuB19JcGnJ");
     	ParseAnalytics.trackAppOpened(getIntent());
     	
        // Click on sign up button
        final Button signUpButton = (Button) findViewById(R.id.signup);
        signUpButton.setOnClickListener(new View.OnClickListener() {

        	@Override
        	public void onClick(View v) {
        		// Get input values
        		final String s_username = username.getText().toString();
        		final String s_password = password.getText().toString();
        		Toast.makeText(getApplicationContext(), s_username, Toast.LENGTH_SHORT).show();
        		Toast.makeText(getApplicationContext(), s_password, Toast.LENGTH_SHORT).show();
        		
        		// Add new User to DataBase
             	ParseObject newUser = new ParseObject("User");
             	newUser.put("username", s_username);
             	newUser.put("password", s_password);
             	newUser.saveInBackground();
        	}
        });
        
    }
}