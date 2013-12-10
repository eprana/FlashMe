package com.qualcomm.QCARSamples.FlashMe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends Activity {

	// Data to get
	final String EXTRA_LOGIN = "user_login";
	final String EXTRA_PASSWORD = "user_password";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        Intent intent = getIntent();
        // Set username on the top of the page
        TextView userName = (TextView) findViewById(R.id.name);
        //TextView score_txt2 = (TextView) findViewById(R.id.score_txt2);
        
        if(intent != null){
        	userName.setText(intent.getStringExtra(EXTRA_LOGIN));
        	//score_txt2.setText(intent.getStringExtra(EXTRA_PASSWORD));
        }
    } 
}
