package com.mnishiguchi.android.criminalintent;

import android.support.v4.app.Fragment;

public class CListActivity extends SingleFragmentActivity
{
	@Override
	protected Fragment createFragment()
	{
		// Return an instance of the fragment that the activity is hosting. 
		return new CListFragment();
	}

}
