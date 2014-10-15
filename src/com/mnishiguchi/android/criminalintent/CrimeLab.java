package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;

/**
 * A singleton class to keep the crime data available no matter what happens
 * with activities, fragments and their lifecycles.
 */
public class CrimeLab
{
	/* STATIC */
	private static CrimeLab sCrimeLab;	
	
	/* INSTANCE VARIABLES */
	private Context mAppContext;
	private ArrayList<Crime> mCrimes;
	
	/** CONSTRUCTOR */
	private CrimeLab(Context appContext)
	{
		mAppContext = appContext;
		mCrimes = new ArrayList<Crime>();
		
		initFakeCrimes();
	}
	
	/**
	 * @param c - This could be an Activity or another Context object like Service.
	 * @return CrimeLab object associated with the current global context. 
	 */
	public static CrimeLab get(Context context)
	{
		if (sCrimeLab == null )  // Only the first time.
		{
			// Create a CrimeLab object associated with the current global context. 
			sCrimeLab = new CrimeLab(context.getApplicationContext() );
		}
		return sCrimeLab;
	}
	
	/**
	 * @return an ArrayList of all the Crimes stored in the CrimeLab.
	 */
	public ArrayList<Crime> getCrimes()
	{
		return mCrimes;
	}
	
	/**
	 * @param id
	 * @return a Crime object associated with the specified id.
	 */
	public Crime getCrime(UUID id)
	{
		for (Crime crime : mCrimes)
		{
			if (crime.getId().equals(id) )
			{
				return crime;
			}
		}
		return null;
	}
	
	/**
	 * Populate the ArrayList with 100 Crime objects.
	 */
	private void initFakeCrimes()
	{
		for (int i = 0; i < 100; i++)
		{
			Crime c = new Crime();
			c.setTitle("Crime #" + i);
			c.setSolved(i%2 == 0);  // Every other one
			mCrimes.add(c);
		}
		
	}

}
