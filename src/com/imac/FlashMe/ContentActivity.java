package com.imac.FlashMe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.imac.FlashMe.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

public class ContentActivity extends Activity implements 
	ActionBar.TabListener,
	ViewPager.OnPageChangeListener {

	private static final String LOGTAG = "ContentActivity";
	private final int EDIT_PROFILE = 3000;
	
	private class TabAction { public int icon; public int icon_in; public String text; 
		public TabAction(int icon, int icon_in, String text) {
			this.icon = icon;
			this.icon_in = icon_in;
			this.text = text;
		}
	}
	
	static ParseUser currentUser = null;
	private ViewPager mViewPager = null;
	private ActionBar actionBar = null;
	private TabsPagerAdapter mAdapter = null;
	private TabAction[] tabs = {new TabAction(R.drawable.menu_profile_bt, R.drawable.menu_profile_bt_in, "Profile"), new TabAction(R.drawable.menu_teams_bt, R.drawable.menu_teams_bt_in, "Teams"), new TabAction(R.drawable.menu_games_bt, R.drawable.menu_games_bt_in, "Games")};
	private Context context = null;
	private LayoutInflater inflater = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
	 	setContentView(R.layout.activity_content);
	 	context = ContentActivity.this;
	 	inflater = LayoutInflater.from(context);
	 	
	 	final FragmentManager fm = getFragmentManager();
	 	mViewPager = (ViewPager) findViewById(R.id.pager);
	 	actionBar = getActionBar();
	 	mAdapter = new TabsPagerAdapter(fm);
	 	mViewPager.setAdapter(mAdapter);
	 	mViewPager.setOffscreenPageLimit(2);
	 	mViewPager.setOnPageChangeListener(this);
	 	
	 	actionBar.setIcon(R.drawable.ic_menu);
	 	actionBar.setDisplayShowTitleEnabled(false);
	 	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
	 	for(TabAction tab : tabs) {
	 		actionBar.addTab(actionBar.newTab().setIcon(tab.icon)./*setText(tab.text).*/setTabListener(this));
	 		actionBar.getTabAt(0).setIcon(tabs[0].icon_in);
	 	}
	 	mViewPager.setCurrentItem(0);
	 	
	 	currentUser = ParseUser.getCurrentUser();
	 	//State 0:offline, 1:online
	 	currentUser.put("state", 1);
	 	currentUser.saveInBackground();
	 	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.actionbar_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_edit_profile:
	        	Intent intent = new Intent(getApplicationContext(), EditActivity.class);
	        	startActivityForResult(intent, EDIT_PROFILE);
	            return true;
	        case R.id.action_send_marker:
	        	// Create an alert box
	        	View alertDialogView = null;
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				MessageAlert msg_a;
				
				msg_a = new MessageAlert();
				alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
				msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
				alertDialogView.setTag(msg_a);
				
				// Choosing the type of message alert
				msg_a.msg.setText(context.getResources().getString(R.string.resend_marker, currentUser.getEmail()));
				
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Send marker again");
				adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	// Going back to the front screen
		          } });
				adb.setPositiveButton("SEND AGAIN", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
	          	   		// Send an e-mail
	     				SendMailToUser mail = new SendMailToUser(context);
	     				String email = currentUser.getEmail();
	                    String subject = "Your marker";
	                    String message = context.getResources().getString(R.string.marker_to_resend, "http://www.pouet.fr");
	                    mail.sendMail(email, subject, message);
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
	            return true;
	        case R.id.action_log_out:
	        	ParseUser.logOut();
	        	finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onPause() {
		Log.d(LOGTAG, "onPause");
		super.onPause();
	 	currentUser.put("state", 0);
	 	currentUser.saveInBackground();
	}
	
	@Override
	protected void onResume() {
		Log.d(LOGTAG, "onResume");
		super.onResume();
	 	currentUser.put("state", 1);
	 	currentUser.saveInBackground();
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle("Log out");
		alertDialog.setMessage("Are you sure you want to log out ?");
		alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User wants to log out
				finish();
			}
		});
		alertDialog.setNegativeButton("CANCEL", null);
		alertDialog.create();
		alertDialog.show();	
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	private class TabsPagerAdapter extends FragmentPagerAdapter {

		public TabsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int index) {
			Fragment fragment;
			switch(index) {
			case 0:
				fragment = new ProfileFragment();
				return fragment;
			case 1:
				fragment = new TeamsFragment();
				return fragment;
			case 2:
				fragment = new GamesFragment();
				//map.put(index, fragment);
				return fragment;
			}
			return null;
		}
		
		@Override
		public int getCount() {
			return 3;
		}		
	}
	
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		Log.d(LOGTAG, "onTabSelected");
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		Log.d(LOGTAG, "onPageSelected");
		getActionBar().setSelectedNavigationItem(position);
		switch(position) {
		case 0:
			actionBar.getTabAt(0).setIcon(tabs[0].icon_in);
			actionBar.getTabAt(1).setIcon(tabs[1].icon);
			actionBar.getTabAt(2).setIcon(tabs[2].icon);
			break;
		case 1:
			actionBar.getTabAt(0).setIcon(tabs[0].icon);
			actionBar.getTabAt(1).setIcon(tabs[1].icon_in);
			actionBar.getTabAt(2).setIcon(tabs[2].icon);
			break;
		case 2:
			actionBar.getTabAt(0).setIcon(tabs[0].icon);
			actionBar.getTabAt(1).setIcon(tabs[1].icon);
			actionBar.getTabAt(2).setIcon(tabs[2].icon_in);
			break;
		}
	}
}