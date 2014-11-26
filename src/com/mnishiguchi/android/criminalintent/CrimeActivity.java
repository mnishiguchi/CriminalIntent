package com.mnishiguchi.android.criminalintent;

import java.util.UUID;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

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

	/**
	 * Determine which interface was inflated, single-pane or two-pane.
	 * @return true if in the two-pane mode, else false.
	 */
	private boolean hasTwoPane()
	{
		// Check whether the layout has a detailFragmentContainer.
		return (findViewById(R.id.detailFragmentContainer) != null);
	}
}

