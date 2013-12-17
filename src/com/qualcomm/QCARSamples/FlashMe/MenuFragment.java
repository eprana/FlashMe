package com.qualcomm.QCARSamples.FlashMe;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MenuFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View mainView = inflater.inflate(R.layout.menu, container, false);		
		ImageButton profile_bt = (ImageButton) mainView.findViewById(R.id.profile_bt);
		ImageButton team_bt = (ImageButton) mainView.findViewById(R.id.team_bt);
		ImageButton game_bt = (ImageButton) mainView.findViewById(R.id.game_bt);
		
//		profile_bt.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//		
	 	return mainView;
		
	}
	
	//On click, changer menu
	// ((MenuActivity)getActivity()).onObjetChoisi(...);
}
