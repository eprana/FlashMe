package com.qualcomm.QCARSamples.FlashMe;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContentFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View mainView = inflater.inflate(R.layout.profile, container, false);		
		return mainView;
	}

	public void setContent(Uri objet){
		//TO DO
		//méthode afficherDetail(Uri objet) qui sera appelée à chaque fois que l'utilisateur choisira un objet.
		//Ce sera cette méthode qui sera chargée de mettre à jour les informations affichées.

	}
}
