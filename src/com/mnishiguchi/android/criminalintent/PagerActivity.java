package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

public class PagerActivity extends FragmentActivity
		implements CrimeFragment.DetailCallbacks
{
	private static final String TAG = "CriminalIntent.PagerActivity";
	
	private ViewPager mViewPager;
	
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate()");
		
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);  // This id is manually defined in res/values/ids.xml
		setContentView(mViewPager);
		
		// Get the list of crimes via the CrimeLab singleton.
		mCrimes = CrimeLab.get(this).getCrimes();
		
		// Configuration.
		setUpPagerAdapter();
		setUpInitialPagerItem();
		setUpEventListener();
	}
	
	private void setUpPagerAdapter()
	{
		Log.d(TAG, "setUpPagerAdapter()");
		FragmentManager fm = getSupportFragmentManager();
		mViewPager.setAdapter( new FragmentStatePagerAdapter(fm) {
			
			@Override
			public int getCount()
			{
				return mCrimes.size();
			}
			
			/**
			 * Returns a CrimeFragment configured to
			 * display the crime at the specified position.
			 */
			@Override
			public Fragment getItem(int position)
			{
				Crime crime = mCrimes.get(position);
				return CrimeFragment.newInstance(crime.getId() );
			}
		});
	}
	
	/**
	 * Sets the initial page to the selected item on the ListFragment.
	 */
	private void setUpInitialPagerItem()
	{
		Log.d(TAG, "setUpInitialPagerItem()");
		UUID crimeid = (UUID) getIntent()
			.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		for (int i = 0; i < mCrimes.size(); i++)
		{
			if (mCrimes.get(i).getId().equals(crimeid) )
			{
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}
	
	private void setUpEventListener()
	{
		Log.d(TAG, "setUpEventListener()");
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			// Invoked when a new page becomes selected.
			@Override
			public void onPageSelected(int position)
			{
				Log.d(TAG, "onPageSelected(...)");

				// Set the new page's crime title.
				Crime crime = mCrimes.get(position);
				if (crime.getTitle() != null)
				{
					setTitle(crime.getTitle() );
				}
			}
			
			// Invoked when the current page is scrolled
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				// Required but not used in this implementation.
			}
			
			@Override
			public void onPageScrollStateChanged(int state)
			{
				// Required but not used in this implementation.
			}
		});
	}
	
	public PagerAdapter getPagerAdapter()
	{
		return mViewPager.getAdapter();
	}
	
	/**
	 * Show a toast message.
	 */
	private void showToast(String msg)
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCrimeUpdated(Crime crime)
	{
		// Required but not used in this implementation.
	}

	@Override
	public void onCrimeDeleted(Crime crime)
	{
		// Required but not used in this implementation.
	}

	@Override
	public void onCrimeAdded(Crime crime)
	{
		// TODO Auto-generated method stub
		
	}
}
