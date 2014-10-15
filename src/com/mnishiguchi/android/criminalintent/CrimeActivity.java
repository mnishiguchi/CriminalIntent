package com.mnishiguchi.android.criminalintent;

import java.util.UUID;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;

public class CrimeActivity extends SingleFragmentActivity
{
	@Override
	protected Fragment createFragment()
	{
		// Retrieve the extra and fetch the Crime.
		UUID crimeId = (UUID) getIntent()
				.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		
		// Return an instance of the fragment that the activity is hosting. 
		return CrimeFragment.newInstance(crimeId);
	}
	
}

