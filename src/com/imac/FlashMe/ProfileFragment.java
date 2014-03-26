package com.imac.FlashMe;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;
import com.imac.FlashMe.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

	private final String LOGTAG = "TeamsFragment";
	
	// Data elements
	private ParseUser currentUser = null;
	private ProgressBar progress = null;
	
	// Layout elements
	private ImageButton refreshButton;
	private ImageView profilePictureView = null;
	private ImageView profileMarkerView = null;
	private TextView totalScoreView;
	private TextView bestScoreValue;
	private TextView victoriesValue;
	private TextView rankValue;
	private TextView defeatsValue;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Log.d(LOGTAG, "onCreateView");
		
		View mainView = inflater.inflate(R.layout.fragment_profile, container, false);
		final Context context = mainView.getContext();
    	
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		refreshButton = (ImageButton) mainView.findViewById(R.id.refresh_bt);
		
		profilePictureView = (ImageView) mainView.findViewById(R.id.profile_picture);
		profileMarkerView = (ImageView) mainView.findViewById(R.id.marker_picture);
		totalScoreView = (TextView) mainView.findViewById(R.id.total_score);
		bestScoreValue = (TextView) mainView.findViewById(R.id.best_score_value);
		rankValue = (TextView) mainView.findViewById(R.id.rank_value);
		defeatsValue = (TextView) mainView.findViewById(R.id.defeats_value);
		victoriesValue = (TextView) mainView.findViewById(R.id.victories_value);

		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadStatistics();
				loadProfilePicture(context);
			}
		});
		
		// Load display data
		loadProfileData(context);
		
		return mainView;
	}
	
	
	public void loadProfileData(final Context context) {

		// Get user's statistics
		loadStatistics();
		
		// Get user's images
		loadProfilePicture(context);
    	loadMarkerImage(context);
	}
	
	private void loadStatistics() {
	    totalScoreView.setText("TOTAL SCORE : "+ String.valueOf(currentUser.getInt("totalScore")));
	    bestScoreValue.setText(String.valueOf(currentUser.getInt("bestScore")));
		rankValue.setText(String.valueOf(currentUser.getInt("rank")));
		defeatsValue.setText(String.valueOf(currentUser.getInt("defeats")));
		victoriesValue.setText(String.valueOf(currentUser.getInt("victories")));
		
		ParseQuery<ParseObject> markerQuery = ParseQuery.getQuery("Marker");
		markerQuery.whereEqualTo("Id", currentUser.getInt("markerId"));
		markerQuery.getFirstInBackground( new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject marker, ParseException e) {
				rankValue.setText(Integer.toString(marker.getInt("rank")));
			}
		});
	}
	
	private void loadProfilePicture(final Context context) {
		currentUser.refreshInBackground(new RefreshCallback() {
			@Override
			public void done(ParseObject object, ParseException e) {
				ParseFile avatarFile = (ParseFile) currentUser.get("avatar");
				avatarFile.getDataInBackground(new GetDataCallback() {
					public void done(byte[] data, ParseException e) {
						if (e != null){
							Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}
						Bitmap avatarBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						// Setting the profile imageView
						profilePictureView.setImageBitmap(avatarBitmap);
						progress.setVisibility(View.GONE);
						profileMarkerView.setVisibility(View.VISIBLE);
						profilePictureView.setVisibility(View.VISIBLE);
					}
				});
			}
		});
	}
	
	private void loadMarkerImage(final Context context) {
		ParseQuery<ParseObject> markerQuery = ParseQuery.getQuery("Marker");
    	markerQuery.whereEqualTo("Id", currentUser.getInt("MarkerId"));
    	markerQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			
			@Override
			public void done(ParseObject marker, ParseException e) {
				ParseFile markerFile = (ParseFile) marker.getParseFile("thumb");
		    	markerFile.getDataInBackground(new GetDataCallback() {
					public void done(byte[] data, ParseException e) {
						if (e != null){
							Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}
						Bitmap markerBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						// Setting the marker imageView
						profileMarkerView.setImageBitmap(markerBitmap);
					}
				});
			}
		});
	}
}