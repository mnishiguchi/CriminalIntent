package com.mnishiguchi.android.criminalintent;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.OrientationEventListener;
import android.view.Window;
import android.view.WindowManager;

public class CrimeCameraActivity extends SingleFragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Hide the status bar and other os-level chrome.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Fragment createFragment()
	{
		return new CrimeCameraFragment();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
}

