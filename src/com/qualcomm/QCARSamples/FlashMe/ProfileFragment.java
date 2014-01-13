package com.qualcomm.QCARSamples.FlashMe;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

	// Data elements
	private ParseUser currentUser = null;
	
	// Layout elements
	private ImageView profilePictureView = null;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.profile, container, false);
		Context context = mainView.getContext();
    	
		// Get current user
		currentUser = ParseUser.getCurrentUser();
		
		// Get layout element for avatar
		profilePictureView = (ImageView) mainView.findViewById(R.id.profile_picture);
		
		// Get user's profile picture with Parse
		this.loadProfilePicture();
		
		//LoadProfile lp = new LoadProfile(context);
    	//lp.execute();
    	
		return mainView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		final Context context = getActivity().getApplicationContext();
		//LoadProfile lp = new LoadProfile(context);
    	//lp.execute();
	}
	
	private void loadProfilePicture() {
		// Parse query
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", currentUser.getUsername());
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			// Find current user
			public void done(ParseUser user, ParseException e) {
			    if (e != null) {
			    	Toast.makeText(getActivity(), "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			    	return;
			    }
		    	ParseFile avatarFile = (ParseFile) user.get("avatar");
		    	try {
					byte[] avatarByteArray = avatarFile.getData();
					Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.length);
					// Setting the imageView
					profilePictureView.setImageBitmap(avatarBitmap);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
}