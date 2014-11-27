package com.mnishiguchi.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * On  a phone - One pane.
 * On a tablet - Two pane.
 */
public class CrimeListActivity extends SingleFragmentActivity
		implements CrimeListFragment.ListCallbacks, CrimeFragment.DetailCallbacks
{
	@Override
	protected Fragment createFragment()
	{
		// Return an instance of the fragment that the activity is hosting. 
		return new CrimeListFragment();
	}
	
	@Override
	protected int getLayoutResId()
	{
		// an alias resource defined in res/values/refs.xml
		return R.layout.activity_masterdetail; 
	}

	@Override
	public void onCrimeAdded(Crime crime)
	{
		// Required but not used in this implementation.
	}
	
	@Override
	public void onCrimeSelected(Crime crime)
	{
		if (Utils.hasTwoPane(this)) // sw600-land
		{
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			
			if (oldDetail != null)
			{
				ft.remove(oldDetail);
			}
			
			if (crime != null)
			{
				Fragment newDetail = CrimeFragment.newInstance(crime.getId());
				ft.add(R.id.detailFragmentContainer, newDetail);
			}
			ft.commit();
		}
		else // Phone
		{
			// Start an instance of CrimePagerActivity
			Intent i = new Intent(this, PagerActivity.class);
			i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
			startActivity(i);
		}
	}

	@Override
	public void onCrimeUpdated(Crime crime)
	{
		FragmentManager fm = getSupportFragmentManager();
		CrimeListFragment listFragment = (CrimeListFragment)
				fm.findFragmentById(R.id.fragmentContainer);
		listFragment.updateUI();
	}

	@Override
	public void onCrimeDeleted(Crime crime)
	{
		// Clear the action bar title.
		setTitle("");
		
		// Clear the detailFragmentContainer.
		removeDetailFragment();
		
		// Access the listFragment.
		FragmentManager fm = getSupportFragmentManager();
		CrimeListFragment listFragment = (CrimeListFragment)fm.findFragmentById(R.id.fragmentContainer);

		// Clear the selection.
		listFragment.clearListSelection();
		
		// Update the listView
		listFragment.updateUI();
	}
	
	@Override
	public void onListItemsDeleted(Crime[] selectedItems)
	{
		// Required but not used in this implementation.
	}

	@Override
	public void onActionMode()
	{
		removeDetailFragment();
	}
	
	private void removeDetailFragment()
	{
		// Remove the detail pane, if any.
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
		
		if (oldDetail != null)
		{
			ft.remove(oldDetail);
		}

		ft.commit();
	}
}
