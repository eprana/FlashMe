package com.qualcomm.QCARSamples.FlashMe;

import java.util.ArrayList;

import com.parse.ParseUser;

import android.R.menu;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ExpandableListView.OnChildClickListener;

public class ContentActivity extends Activity{

	static ParseUser currentUser = null;
	private Menu menu;
	private ExpandableListView sList;
	private SettingsAdapter sAdapter;
	
	// Fragments
	private String mFragment;
	private ProfileFragment mProfileFragment;
	private TeamsFragment mTeamsFragment;
	private GamesFragment mGamesFragment;
	private static String TAG_PROFILE = "PROFILE_FRAGMENT";
	private static String TAG_TEAMS = "TEAMS_FRAGMENT";
	private static String TAG_GAMES = "GAMES_FRAGMENT";
	
	private void setupFragments() {
		final FragmentManager fm = getFragmentManager();
		
		mProfileFragment = (ProfileFragment) fm.findFragmentByTag(TAG_PROFILE);
		if (mProfileFragment == null) {
			mProfileFragment = new ProfileFragment();
		}
		mTeamsFragment = (TeamsFragment) fm.findFragmentByTag(TAG_TEAMS);
		if (mTeamsFragment == null) {
			mTeamsFragment = new TeamsFragment();
		}
		mGamesFragment = (GamesFragment) fm.findFragmentByTag(TAG_GAMES);
		if (mGamesFragment == null) {
			mGamesFragment = new GamesFragment();
		}		
	}

	private void showFragment(final android.app.Fragment fragment) {
		if (fragment == null){
			return;
		}
		final FragmentManager fm = getFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();		
		try {
			ft.replace(R.id.maincontainer, fragment, fragment.getTag());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ft.commit();
	}
	
	// Menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		System.out.println("CREATE MENUUUUUUUUUUUUUUUU");
	    // Inflate the menu items for use in the action bar
		this.menu = menu;
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_profile:
	        	item.setIcon(R.drawable.menu_profile_bt);
	        	menu.getItem(1).setIcon(R.drawable.menu_teams_bt_in);
	        	menu.getItem(2).setIcon(R.drawable.menu_games_bt_in);
	        	showFragment(this.mProfileFragment);
	            return true;
	        case R.id.action_teams:
	        	item.setIcon(R.drawable.menu_teams_bt);
	        	menu.getItem(0).setIcon(R.drawable.menu_profile_bt_in);
	        	menu.getItem(2).setIcon(R.drawable.menu_games_bt_in);
	        	showFragment(this.mTeamsFragment);
	            return true;
	        case R.id.action_games:
	        	item.setIcon(R.drawable.menu_games_bt);
	        	menu.getItem(1).setIcon(R.drawable.menu_teams_bt_in);
	        	menu.getItem(0).setIcon(R.drawable.menu_profile_bt_in);
	        	showFragment(this.mGamesFragment);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
	 	setContentView(R.layout.content);

	 	
	 	// Don't display logo and title in the menu
	 	getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        //getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
    	// Set username on top of the page
	 	currentUser = ParseUser.getCurrentUser();
    	TextView userName = (TextView) findViewById(R.id.name);
    	userName.setText(currentUser.getUsername());
	 	
	 	// Set fragment
	 	if (savedInstanceState != null)
	 		mFragment = savedInstanceState.getString("fragment");
	 	else
	 		mFragment = getIntent().getStringExtra("fragment");
	 	
	 	if(mFragment == null) {
	 		mFragment = "ProfileFragment";
	 	}
	 	
		setupFragments();

		if(mFragment != null) {
			if (mFragment.equals("ProfileFragment")) {
				showFragment(this.mProfileFragment);
			} else if (mFragment.equals("TeamsFragment")) {
				showFragment(this.mTeamsFragment);
			} else if (mFragment.equals("GamesFragment")) {
				showFragment(this.mGamesFragment);
			}
		}
		
    	// Settings button expandable list
    	sList = (ExpandableListView)findViewById(R.id.s_list);
    	sList.setDivider(null);
    	ArrayList<Settings> settings_bt = new ArrayList<Settings>();
		Settings setting = new Settings(getResources().getDrawable(R.drawable.settings_bt));
		ArrayList<SettingsButton> buttons = new ArrayList<SettingsButton>();
		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.edit_bt), "Edit profile"));
		buttons.add(new SettingsButton(setting, getResources().getDrawable(R.drawable.logout_bt), "Log out"));
		setting.setSettingsButtons(buttons);
		settings_bt.add(setting);
		sAdapter = new SettingsAdapter(this, settings_bt);
		//backend = new BackEnd(this);

		sList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if(groupPosition == 0 && childPosition == 1){
					//Log out
					ParseUser.logOut();
					finish();
				}
				else if(groupPosition == 0 && childPosition == 0){
					//Edit profile
					Intent intent = new Intent(getApplicationContext(), EditActivity.class);
	            	startActivity(intent);
				}
				
				return false;
			}
		});
		
		sList.setAdapter(sAdapter);
		
		final TextView top_line = (TextView) findViewById(R.id.top_line_light);
		top_line.setText(R.string.my_profile);
		
 	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment", mFragment != null ? mFragment : "");
		super.onSaveInstanceState(outState);
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
}