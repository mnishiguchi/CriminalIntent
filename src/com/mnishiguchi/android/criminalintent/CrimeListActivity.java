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
		return R.layout.activity_twopane;
	}

	@Override
	public void onCrimeSelected(Crime crime)
	{
		if (hasTwoPane()) // Tablet
		{
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			Fragment newDetail = CrimeFragment.newInstance(crime.getId());
			
			if (oldDetail != null)
			{
				ft.remove(oldDetail);
			}
			
			ft.add(R.id.detailFragmentContainer, newDetail);
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
	
	/**
	 * Determine which interface was inflated, single-pane or two-pane.
	 * @return true if in the two-pane mode, else false.
	 */
	private boolean hasTwoPane()
	{
		// Check whether the layout has a detailFragmentContainer.
		return (findViewById(R.id.detailFragmentContainer) != null);
	}

	@Override
	public void onCrimeDeleted(Crime crime)
	{
		removeDetailFragment();
	}
	
	@Override
	public void onCrimeDeleted(Crime[] selectedItems)
	{
		// removeDetailFragment();
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
