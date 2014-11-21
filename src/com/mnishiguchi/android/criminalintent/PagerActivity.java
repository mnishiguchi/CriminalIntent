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

public class PagerActivity extends FragmentActivity
{
	private ViewPager mViewPager;
	
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
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
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position)
			{
				Crime crime = mCrimes.get(position);
				
				if (crime.getTitle() != null)  // If the title is initialized already...
				{
					setTitle(crime.getTitle() );
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }  // unused
			
			@Override
			public void onPageScrollStateChanged(int arg0) { }  // unused
		});
	}
	
	public PagerAdapter getPagerAdapter()
	{
		return mViewPager.getAdapter();
	}
}
