package com.imac.FlashMe;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.imac.FlashMe.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

	private static final String LOGTAG = "TeamsFragment";
	
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private static ImageView profilePictureView = null;
	private static ImageView profileMarkerView = null;
	private static TextView totalScoreView;
	private static TextView bestScoreValue;
	private static TextView victoriesValue;
	private static TextView rankValue;
	private static TextView defeatsValue;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_profile, container, false);
		Context context = mainView.getContext();
    	
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
		profilePictureView = (ImageView) mainView.findViewById(R.id.profile_picture);
		profileMarkerView = (ImageView) mainView.findViewById(R.id.marker_picture);
		totalScoreView = (TextView) mainView.findViewById(R.id.total_score);
		bestScoreValue = (TextView) mainView.findViewById(R.id.best_score_value);
		rankValue = (TextView) mainView.findViewById(R.id.rank_value);
		defeatsValue = (TextView) mainView.findViewById(R.id.defeats_value);
		victoriesValue = (TextView) mainView.findViewById(R.id.victories_value);
				
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
			//progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			loadProfileData(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//progress.setVisibility(View.GONE);
		}
	}
	
	public static void loadProfileData(final Context context) {
		// Parse query for profile picture
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", currentUser.getUsername());
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			// Find current user
			public void done(final ParseUser user, ParseException e) {
			    if (e != null) {
			    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			    	return;
			    }

			    totalScoreView.setText("TOTAL SCORE : "+ String.valueOf(user.getInt("totalScore")));
			    bestScoreValue.setText(String.valueOf(user.getInt("bestScore")));
				rankValue.setText(String.valueOf(user.getInt("rank")));
				defeatsValue.setText(String.valueOf(user.getInt("defeats")));
				victoriesValue.setText(String.valueOf(user.getInt("victories")));
				
		    	ParseFile avatarFile = (ParseFile) user.get("avatar");
				avatarFile.getDataInBackground(new GetDataCallback() {
					public void done(byte[] data, ParseException e) {
						if (e != null){
							Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						// Setting the profile imageView
						profilePictureView.setImageBitmap(avatarBitmap);
					}
				});
				
		    	ParseQuery<ParseObject> markerQuery = ParseQuery.getQuery("Marker");
		    	markerQuery.whereEqualTo("Id", user.getInt("MarkerId"));
		    	markerQuery.getFirstInBackground(new GetCallback<ParseObject>() {
					
					@Override
					public void done(ParseObject marker, ParseException e) {
						ParseFile markerFile = (ParseFile) marker.getParseFile("image");
				    	markerFile.getDataInBackground(new GetDataCallback() {
							public void done(byte[] data, ParseException e) {
								if (e != null){
									Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
									return;
								}
								Bitmap markerBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
								// Setting the marker imageView
								profileMarkerView.setImageBitmap(markerBitmap);
								progress.setVisibility(View.GONE);
								profileMarkerView.setVisibility(View.VISIBLE);
								profilePictureView.setVisibility(View.VISIBLE);
							}
						});
					}
				});
			}
		});
		
//		// Parse query for marker
//		ParseQuery<ParseUser> markerQuery = ParseUser.getQuery();
//		markerQuery.whereEqualTo("username", currentUser.getUsername());
//		markerQuery.getFirstInBackground(new GetCallback<ParseUser>() {
//			// Find current user
//			public void done(ParseUser user, ParseException e) {
//			    if (e != null) {
//			    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
//			    	return;
//			    }
//
//			}
//		});
	}
}