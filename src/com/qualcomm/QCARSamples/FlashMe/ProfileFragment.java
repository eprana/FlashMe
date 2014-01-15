package com.qualcomm.QCARSamples.FlashMe;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private static ImageView profilePictureView = null;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.profile, container, false);
		Context context = mainView.getContext();
    	
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
		profilePictureView = (ImageView) mainView.findViewById(R.id.profile_picture);
		
		// Load fragment data
		LoadProfile lp = new LoadProfile(context);
    	lp.execute();
    	
		return mainView;
	}
	
	private static class LoadProfile extends AsyncTask<Void, Integer, Void> {

		private Context context;
		
		public LoadProfile(Context context){
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			loadProfilePicture(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);
		}
	}
	
	public static void loadProfilePicture(final Context context) {
		// Parse query
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", currentUser.getUsername());
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			// Find current user
			public void done(ParseUser user, ParseException e) {
			    if (e != null) {
			    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			    	return;
			    }
		    	ParseFile avatarFile = (ParseFile) user.get("avatar");
				avatarFile.getDataInBackground(new GetDataCallback() {
					public void done(byte[] data, ParseException e) {
						if (e != null){
							Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						// Setting the imageView
						profilePictureView.setImageBitmap(avatarBitmap);
					}
				});
			}
		});
	}
}