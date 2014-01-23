package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.mail.internet.AddressException;

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
	ViewPager.OnPageChangeListener,
	TeamsFragment.OnTeamSelectedListener {

	private class TabAction { public int icon; public String text; 
		public TabAction(int icon, String text) {
			this.icon = icon;
			this.text = text;
		}
	}
	
	static ParseUser currentUser = null;
	private ViewPager mViewPager;
	private TabsPagerAdapter mAdapter;
	private TabAction[] tabs = {new TabAction(R.drawable.menu_profile_bt, "Profile"), new TabAction(R.drawable.menu_teams_bt, "Teams"), new TabAction(R.drawable.menu_games_bt, "Games")};
	private Menu menu;
	private ExpandableListView sList;
	private SettingsAdapter sAdapter;
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    // Inflate the menu items for use in the action bar
//		this.menu = menu;
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.layout.main_activity_actions, menu);
//	    return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle presses on the action bar items
//	    switch (item.getItemId()) {
//	        case R.id.action_profile:
//	        	item.setIcon(R.drawable.menu_profile_bt);
//	        	menu.getItem(1).setIcon(R.drawable.menu_teams_bt_in);
//	        	menu.getItem(2).setIcon(R.drawable.menu_games_bt_in);
//	        	//showFragment(ProfileFragment.class);
//	            return true;
//	        case R.id.action_teams:
//	        	item.setIcon(R.drawable.menu_teams_bt);
//	        	menu.getItem(0).setIcon(R.drawable.menu_profile_bt_in);
//	        	menu.getItem(2).setIcon(R.drawable.menu_games_bt_in);
//	        	//showFragment(TeamsFragment.class);
//	            return true;
//	        case R.id.action_games:
//	        	item.setIcon(R.drawable.menu_games_bt);
//	        	menu.getItem(1).setIcon(R.drawable.menu_teams_bt_in);
//	        	menu.getItem(0).setIcon(R.drawable.menu_profile_bt_in);
//	        	//showFragment(GamesFragment.class);
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.content);

	 	//context = ContentActivity.this;
	 	//final LayoutInflater inflater = LayoutInflater.from(context);
	 	
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
//    	
//    	mFragmentClass = ProfileFragment.class;
//	 	
//	 	// Set fragment
//	 	if (savedInstanceState != null)
//	 		mFragment = savedInstanceState.getString("fragment");
//	 	else
//	 		mFragment = getIntent().getStringExtra("fragment");
//	 	
//	 	if(mFragment == null) {
//	 		mFragment = "ProfileFragment";
//	 	}
//	 	
		//setupFragments();
	 	//showFragment(mFragmentClass);
//		if(mFragment != null) {
//			if (mFragment.equals("ProfileFragment")) {
//				showFragment(this.mProfileFragment);
//			} else if (mFragment.equals("TeamsFragment")) {
//				showFragment(this.mTeamsFragment);
//			} else if (mFragment.equals("GamesFragment")) {
//				showFragment(this.mGamesFragment);
//			}
//		}
		
    	// Settings button expandable list

//		sList = (ExpandableListView)findViewById(R.id.s_list);
//		sList.setDivider(null);
//		ArrayList<Settings> settings_bt = new ArrayList<Settings>();
//		Settings setting = new Settings(getResources().getDrawable(R.drawable.settings_bt));
//		ArrayList<SettingsButton> buttons = new ArrayList<SettingsButton>();
//		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.edit_bt), "Edit profile"));
//		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.edit_bt), "Send marker"));
//		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.logout_bt), "Log out"));
//		setting.setSettingsButtons(buttons);
//		settings_bt.add(setting);
//		sAdapter = new SettingsAdapter(this, settings_bt);
//		//backend = new BackEnd(this);
//
//		sList.setOnChildClickListener(new OnChildClickListener() {
//			
//			@Override
//			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//				if(groupPosition == 0 && childPosition == 0){
//					//Edit profile
//					Intent intent = new Intent(getApplicationContext(), EditActivity.class);
//	            	startActivity(intent);
//				}
//				else if(groupPosition == 0 && childPosition == 1){
//					//Send marker
//					
//					// Ask confirmation
//     				// Create an alert box
//    				AlertDialog.Builder adb = new AlertDialog.Builder(context);
//    				MessageAlert msg_a;
//    				
//    				if (alertDialogView == null) {
//    					msg_a = new MessageAlert();
//    					alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
//    					msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
//    					alertDialogView.setTag(msg_a);
//    				} else {
//    					msg_a = (MessageAlert) alertDialogView.getTag();
//    	            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
//    					adbParent.removeView(alertDialogView);
//    				}
//    				
//    				// Choosing the type of message alert
//    				msg_a.msg.setText(context.getResources().getString(R.string.resend_marker, currentUser.getEmail()));
//    				
//    				// Filling the alert box
//    				adb.setView(alertDialogView);
//    				adb.setTitle("Send marker again");
//					adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//			            public void onClick(DialogInterface dialog, int which) {
//			            	// Going back to the front screen
//			          } });
//    				adb.setPositiveButton("SEND AGAIN", new DialogInterface.OnClickListener() {
//    		            public void onClick(DialogInterface dialog, int which) {
//    	          	   		// Send an e-mail
//    	     				SendMailToUser mail = new SendMailToUser(context);
//    	     				String email = currentUser.getEmail();
//    	                    String subject = "Your marker";
//    	                    String message = context.getResources().getString(R.string.marker_to_resend, "http://www.pouet.fr");
//    	                    mail.sendMail(email, subject, message);
//    		        } });
//    				
//    				// Showing the alert box
//    		        adb.create();
//    				adb.show();				
//				}
//				else if(groupPosition == 0 && childPosition == 2){
//					//Log out
//					ParseUser.logOut();
//					finish();
//				}				
//			}
//			return null;
//		}
//		
//		sList.setAdapter(sAdapter);
//		
//		final TextView top_line = (TextView) findViewById(R.id.top_line_light);
//		top_line.setText(R.string.my_profile);
//		
	}
	
	public class Settings {
		private Drawable settingsBt;
		private ArrayList<SettingsButton> sButtons;

		public Settings(Drawable bt) {
			super();
			this.settingsBt = bt;
			this.sButtons = new ArrayList<SettingsButton>();
		}

		//Getters & Setters
		public Drawable getDrawable() {	return settingsBt; }
		public ArrayList<SettingsButton> getSettingsButtons() { return sButtons; }
		public void setSettingsButtons(ArrayList<SettingsButton> buttons) { this.sButtons = buttons; }
	}
	
	
	public class SettingsButton {

		private Settings settingsBt;
		private Drawable picto;
		private String picto_tx;

		public SettingsButton(Settings bt, Drawable picto, String picto_tx) {
			super();
			this.settingsBt = bt;
			this.picto = picto;
			this.picto_tx = picto_tx;
		}

		//Getters & Setters
		public Drawable getDrawable(){ return this.picto; }
		public void setDrawable(Drawable picto){ this.picto = picto; }
		public String getTextPicto() { return picto_tx;	}
		public void setTextPicto(String picto_tx) { this.picto_tx = picto_tx; }

	}
	
	public class SettingsAdapter extends BaseExpandableListAdapter {

		private Context context;
		private ArrayList<Settings> settings;
		private LayoutInflater inflater;

		public SettingsAdapter(Context context, ArrayList<Settings> button) {
			this.context = context;
			this.settings = button;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public boolean areAllItemsEnabled() { return true; }
		public Object getChild(int gPosition, int cPosition) { return settings.get(gPosition).getSettingsButtons().get(cPosition); }
		public long getChildId(int gPosition, int cPosition) { return cPosition; }

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			final SettingsButton sButton = (SettingsButton) getChild(groupPosition, childPosition);
			BViewHolder bViewHolder;

	        if (convertView == null) {
	        	bViewHolder = new BViewHolder();
	            convertView = inflater.inflate(R.layout.settings_buttons, null);
	            bViewHolder.picto = (ImageView) convertView.findViewById(R.id.picto);
	            bViewHolder.picto_tx = (TextView) convertView.findViewById(R.id.picto_tx);
	            convertView.setTag(bViewHolder);
	        } else bViewHolder = (BViewHolder) convertView.getTag();

	        bViewHolder.picto.setImageDrawable(sButton.getDrawable());
	        bViewHolder.picto_tx.setText(sButton.getTextPicto());

	        return convertView;
		}

		public int getChildrenCount(int gPosition) { return settings.get(gPosition).getSettingsButtons().size(); }
		public Object getGroup(int gPosition) { return settings.get(gPosition); }
		public int getGroupCount() { return settings.size(); }
		public long getGroupId(int gPosition) { return gPosition; }

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			GViewHolder gholder;
			Settings settings_bt = (Settings) getGroup(groupPosition);

	        if (convertView == null) {
	        	gholder = new GViewHolder();
	        	convertView = inflater.inflate(R.layout.settings_row, null);
	        	gholder.settings_bt = (ImageView) convertView.findViewById(R.id.settings_bt);
	        	convertView.setTag(gholder);
	        } else gholder = (GViewHolder) convertView.getTag();

	        gholder.settings_bt.setImageDrawable(settings_bt.getDrawable());
	        return convertView;
		}

		public boolean hasStableIds() {	return true; }

		public boolean isChildSelectable(int arg0, int arg1) { 	return true; }
		
		class GViewHolder {
			public ImageView settings_bt;
		}

		class BViewHolder {
			public ImageView picto;
			public TextView picto_tx;
		}
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

	@Override
	public void onTeamSelected(int index) {
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
		getActionBar().setSelectedNavigationItem(position);
		
	}
}