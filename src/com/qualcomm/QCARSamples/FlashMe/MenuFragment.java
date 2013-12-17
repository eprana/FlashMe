package com.qualcomm.QCARSamples.FlashMe;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View mainView = inflater.inflate(R.layout.menu, container, false);		
		return mainView;
	}
	
	//On click, changer menu
	// ((MenuActivity)getActivity()).onObjetChoisi(...);
}
