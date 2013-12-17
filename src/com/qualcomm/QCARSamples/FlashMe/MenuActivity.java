package com.qualcomm.QCARSamples.FlashMe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MenuActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.menu_fragment);
	 
    }
    
    public void onItemSelected(Uri object) {
    	// Getting the changing content fragment
    	ContentFragment viewer = (ContentFragment) getFragmentManager().findFragmentById(R.id.content_fragment);
     
    	// If it does not exist, we create it by lauching its activity
        if (viewer == null || !viewer.isInLayout()) {
        	Intent contentIntent = new Intent(getApplicationContext(), ContentActivity.class);
        	contentIntent.setData(object);
        	startActivity(contentIntent);
        }
        // Else we update the user's choice
        else viewer.setContent(object);
    }
    
    
}
