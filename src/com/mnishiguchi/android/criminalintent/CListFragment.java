package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class CListFragment extends ListFragment
{
	/* INSTANCE VARIABLES */
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Change what is displayed on the hosting activity's action bar.
		getActivity().setTitle(R.string.crimes_title);
		
		// Get the CrimeLab singleton and then get the list of crimes.
		mCrimes = CrimeLab.get(getActivity() ).getCrimes();
	}
	
	/* Note: ListFragments come with a default onCreateView() method.
		The default implementation of a ListFragment inflates a layout that
		defines a full screen ListView. */

}
