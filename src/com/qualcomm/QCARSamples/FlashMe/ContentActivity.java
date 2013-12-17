package com.qualcomm.QCARSamples.FlashMe;

import android.app.Activity;
import android.os.Bundle;

public class ContentActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
		 	setContentView(R.layout.content);
		 	
		 	//setContent(Uri objet);
 	}
}

//OLD PROFILE ACTIVITY TO RE-USE
//package com.qualcomm.QCARSamples.FlashMe;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.TextView;
//
//public class ProfileActivity extends Activity {
//
//    // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
//	// Data to get
//	final String EXTRA_LOGIN = "user_login";
//	final String EXTRA_PASSWORD = "user_password";
//    // ------------------------------------------------------------------------------------
//	
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//	 	setContentView(R.layout.profile_fragment);        
//                
//        // -------------------- TO CHANGE WITH THE BDD (setting the username on top of the page)
//        	Intent intent = getIntent();
//        	
//        	TextView userName = (TextView) findViewById(R.id.name);
//        
//	        if(intent != null){
//	        	userName.setText(intent.getStringExtra(EXTRA_LOGIN));
//	        	//score_txt2.setText(intent.getStringExtra(EXTRA_PASSWORD));
//	        }
//	    // --------------------------------------------------------------------------------------
//    } 
//}
