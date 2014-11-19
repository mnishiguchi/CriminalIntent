package com.mnishiguchi.android.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeCameraActivity extends SingleFragmentActivity
{
	@Override
	protected Fragment createFragment()
	{
		return new CrimeCameraFragment();
	}
}

