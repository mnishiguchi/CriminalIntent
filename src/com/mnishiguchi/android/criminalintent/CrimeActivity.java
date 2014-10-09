package com.mnishiguchi.android.criminalintent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;

public class CrimeActivity extends FragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crime);
		
		/*FragmentManager - Maintains a back stack of fragment transactions.
		 * FragmentTransaction - Used to add, remove, attach, detach,
		 * or replace fragments in the fragment list.
		 * */
		// Get a FragmentManager
		FragmentManager fm = getSupportFragmentManager();
		// Get a reference to the fragment list associated with the fragment_container.
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		// Check if there is already a fragment in the fragment list.
		if (fragment == null)
		{
			// Create a new Fragment and a new FragmentTransaction that adds the Fragment to the list.
			fragment = new CrimeFragment();
			fm.beginTransaction()  // Create a new FragmentTransaction.
					.add(R.id.fragment_container, fragment)  // Include one add operation in it.
					.commit();  // Then commit it.
		}
	}

}  // end class