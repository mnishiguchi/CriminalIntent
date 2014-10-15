package com.mnishiguchi.android.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * A generic superclass of a single-fragment activity.
 * Subclasses of SingleFragmentActivity must implement createFragment()
 * to return an instance of the fragment that the activity is hosting.
 */
public abstract class SingleFragmentActivity extends FragmentActivity
{
	/** Return an instance of the fragment that the activity is hosting. */
	protected abstract Fragment createFragment();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		
		/*FragmentManager - Maintains a back stack of fragment transactions. */
		/*FragmentTransaction - Used to add, remove, attach, detach,
				or replace fragments in the fragment list. */
		
		// Get a FragmentManager
		FragmentManager fm = getSupportFragmentManager();
		
		// Get a reference to the fragment list associated with the fragment_container.
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		
		// Check if there is already a fragment in the fragment list.
		if (fragment == null)
		{
			fragment = createFragment();  // Create a new Fragment.
			
			// Add the Fragment to the list.
			fm.beginTransaction()
					.add(R.id.fragment_container, fragment)
					.commit();
		}
	}
}
