package com.qualcomm.QCARSamples.FlashMe;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

	public static String TAG="TAG_PROFILE";
	// Data elements
	private static ParseUser currentUser = null;
	private static ProgressBar progress = null;
	
	// Layout elements
	private static ImageView profilePictureView = null;
	private static ImageView profileMarkerView = null;
	private static TextView totalScoreView;
	private static TextView bestScoreView;
	private static TextView victoriesView;
	private static TextView rankView;
	private static TextView defeatsView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.profile, container, false);
		Context context = mainView.getContext();
    	
		// Initialize members
		currentUser = ParseUser.getCurrentUser();
		progress = (ProgressBar) mainView.findViewById(R.id.progressBar);
		
		profilePictureView = (ImageView) mainView.findViewById(R.id.profile_picture);
		profileMarkerView = (ImageView) mainView.findViewById(R.id.profile_marker);
		totalScoreView = (TextView) mainView.findViewById(R.id.total_score);
		bestScoreView = (TextView) mainView.findViewById(R.id.best_score_txt);
		rankView = (TextView) mainView.findViewById(R.id.rank_txt);
		defeatsView = (TextView) mainView.findViewById(R.id.defeats_txt);
		victoriesView = (TextView) mainView.findViewById(R.id.victories_txt);
				
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
			loadProfileData(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);
		}
	}
	
	public static void loadProfileData(final Context context) {
		// Parse query for profile picture
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", currentUser.getUsername());
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			// Find current user
			public void done(ParseUser user, ParseException e) {
			    if (e != null) {
			    	Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			    	return;
			    }

			    totalScoreView.setText(Html.fromHtml(context.getResources().getString(R.string.total_score_txt )+" : "+ user.getInt("totalScore")));
			    bestScoreView.setText(Html.fromHtml(context.getResources().getString(R.string.best_score_txt, user.getInt("bestScore"))));
				rankView.setText(Html.fromHtml(context.getResources().getString(R.string.rank_txt, user.getInt("rank"))));
				defeatsView.setText(Html.fromHtml(context.getResources().getString(R.string.defeats_txt, user.getInt("defeats"))));
				victoriesView.setText(Html.fromHtml(context.getResources().getString(R.string.victories_txt, user.getInt("victories"))));
				
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
				
		    	ParseFile markerFile = (ParseFile) user.get("marker");
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