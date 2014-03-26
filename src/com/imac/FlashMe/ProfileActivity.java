package com.imac.FlashMe;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends Activity {

	private Context context;
	private LayoutInflater inflater;

	// Data elements
	private static ParseUser currentUser = null;
	private static ParseUser profileUser = null;
	private static String profileUserId;
	private ProgressBar progress = null;

	// Layout elements
	private ImageButton refreshButton;
	private TextView userNameProfileView;
	private ImageView profilePictureView = null;
	private ImageView markerPictureView = null;	
	private TextView totalScoreView;
	private TextView bestScoreValue;
	private TextView victoriesValue;
	private TextView rankValue;
	private TextView defeatsValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		context = ProfileActivity.this;
		inflater = LayoutInflater.from(context);
		Intent intent = getIntent();

		getActionBar().setIcon(R.drawable.ic_menu);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true); 
		getActionBar().setHomeButtonEnabled(true);

		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		refreshButton = (ImageButton) this.findViewById(R.id.refresh_bt);
		progress = (ProgressBar) this.findViewById(R.id.progressBar);
		userNameProfileView = (TextView) this.findViewById(R.id.top_line_light);
		profilePictureView = (ImageView) this.findViewById(R.id.profile_picture);
		markerPictureView = (ImageView) this.findViewById(R.id.marker_picture);
		totalScoreView = (TextView) this.findViewById(R.id.total_score);
		bestScoreValue = (TextView) this.findViewById(R.id.best_score_value);
		rankValue = (TextView) this.findViewById(R.id.rank_value);
		defeatsValue = (TextView) this.findViewById(R.id.defeats_value);
		victoriesValue = (TextView) this.findViewById(R.id.victories_value);

		profileUserId = intent.getStringExtra("USER");
		ParseQuery<ParseUser> query =  ParseUser.getQuery();
		query.whereEqualTo("objectId", profileUserId);
		query.getFirstInBackground(new GetCallback<ParseUser>(){
			@Override
			public void done(ParseUser user, ParseException e) {
				profileUser = user;
				if(profileUser != null && userNameProfileView != null) {
					userNameProfileView.setText(String.valueOf(profileUser.getString("username") + "'s profile"));

					// Load display data
					loadProfileData(context);
				}

			}
		});

		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadStatistics();
				loadProfilePicture(context);
			}
		});


	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void loadProfileData(final Context context) {

		// Get user's statistics
		loadStatistics();

		// Get user's images
		loadProfilePicture(context);
		loadMarkerImage(context);
	}

	private void loadStatistics() {
		totalScoreView.setText("TOTAL SCORE : "+ String.valueOf(profileUser.getInt("totalScore")));
		bestScoreValue.setText(String.valueOf(profileUser.getInt("bestScore")));
		rankValue.setText(String.valueOf(profileUser.getInt("rank")));
		defeatsValue.setText(String.valueOf(profileUser.getInt("defeats")));
		victoriesValue.setText(String.valueOf(profileUser.getInt("victories")));

		ParseQuery<ParseObject> markerQuery = ParseQuery.getQuery("Marker");
		markerQuery.whereEqualTo("Id", profileUser.getInt("markerId"));
		markerQuery.getFirstInBackground( new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject marker, ParseException e) {
				rankValue.setText(Integer.toString(marker.getInt("rank")));
			}
		});
	}

	private void loadProfilePicture(final Context context) {
		profileUser.refreshInBackground(new RefreshCallback() {
			@Override
			public void done(ParseObject object, ParseException e) {
				ParseFile avatarFile = (ParseFile) profileUser.get("avatar");
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
						markerPictureView.setVisibility(View.VISIBLE);
						profilePictureView.setVisibility(View.VISIBLE);
					}
				});
			}
		});progress.setVisibility(View.GONE);
	}

	private void loadMarkerImage(final Context context) {
		ParseQuery<ParseObject> markerQuery = ParseQuery.getQuery("Marker");
		markerQuery.whereEqualTo("Id", profileUser.getInt("MarkerId"));
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
						//progress.setVisibility(View.GONE);
						markerPictureView.setImageBitmap(markerBitmap);
						//markerPictureView.setVisibility(View.VISIBLE);
					}
				});
			}
		});
	}
}
