package com.qualcomm.QCARSamples.FlashMe;

import javax.mail.internet.AddressException;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.R.menu;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ExpandableListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ExpandableListView.OnChildClickListener;

public class ContentActivity extends Activity implements 
	ActionBar.TabListener,
	ViewPager.OnPageChangeListener
	/*TeamsFragment.OnTeamSelectedListener*/ {

	private class TabAction { public int icon; public String text; 
		public TabAction(int icon, String text) {
			this.icon = icon;
			this.text = text;
		}
	}
	
	static ParseUser currentUser = null;
	private ViewPager mViewPager = null;
	private TabsPagerAdapter mAdapter = null;
	private TabAction[] tabs = {new TabAction(R.drawable.menu_profile_bt, "Profile"), new TabAction(R.drawable.menu_teams_bt, "Teams"), new TabAction(R.drawable.menu_games_bt, "Games")};
	private Context context = null;
	private LayoutInflater inflater = null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_edit_profile:
	        	Intent intent = new Intent(getApplicationContext(), EditActivity.class);
	        	startActivity(intent);
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
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.content);
	 	context = ContentActivity.this;
	 	inflater = LayoutInflater.from(context);
	 	
	 	final FragmentManager fm = getFragmentManager();
	 	mViewPager = (ViewPager) findViewById(R.id.pager);
	 	final ActionBar actionBar = getActionBar();
	 	mAdapter = new TabsPagerAdapter(fm);
	 	mViewPager.setAdapter(mAdapter);
	 	mViewPager.setOffscreenPageLimit(2);
	 	mViewPager.setOnPageChangeListener(this);
	 	
	 	//getActionBar().setDisplayShowHomeEnabled(false);
	 	actionBar.setDisplayShowTitleEnabled(false);
	 	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
	 	for(TabAction tab : tabs) {
	 		actionBar.addTab(actionBar.newTab().setIcon(tab.icon).setText(tab.text).setTabListener(this));
	 	}
	 	mViewPager.setCurrentItem(0);
	 	
    	// Set username on top of the page
//	 	currentUser = ParseUser.getCurrentUser();
//    	TextView userName = (TextView) findViewById(R.id.name);
//    	userName.setText(currentUser.getUsername());	
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
			switch(index) {
			case 0:
				return new ProfileFragment();
			case 1:
				return new TeamsFragment();
			case 2:
				return new GamesFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 3;
		}		
	}
	
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}	

//	@Override
//	public void onTeamSelected(int index) {
//		// TODO Auto-generated method stub
//		
//	}

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
		getActionBar().setSelectedNavigationItem(position);
		
	}
}