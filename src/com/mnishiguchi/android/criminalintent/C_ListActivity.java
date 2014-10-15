package com.mnishiguchi.android.criminalintent;

import android.support.v4.app.Fragment;

public class C_ListActivity extends SingleFragmentActivity
{
	@Override
	protected Fragment createFragment()
	{
		// Return an instance of the fragment that the activity is hosting. 
		return new C_ListFragment();
	}

}
