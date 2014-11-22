package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * A singleton class to keep the crime data available no matter what happens
 * with activities, fragments and their lifecycles.
 */
public class CrimeLab
{
	private static final String TAG = "tag_CrimeLab";

	private static final String FILENAME = "crimes.json";
	
	/** Hold an instance of the  CrimeLab. */
	private static CrimeLab sCrimeLab;	
	
	/* INSTANCE VARIABLES */
	private Context mAppContext;
	private ArrayList<Crime> mCrimes;
	private CrimeJSONSerializer mSerializer;
	
	/** Constructor. */
	private CrimeLab(Context appContext)
	{
		mAppContext = appContext;
		mSerializer = new CrimeJSONSerializer(mAppContext, FILENAME);
		
		// Load crimes from the file system.
		try
		{
			mCrimes = mSerializer.loadCrimes();
			Toast.makeText(mAppContext, "Crimes successfully loaded.",
					Toast.LENGTH_SHORT).show();
		}
		catch (Exception e)
		{
			mCrimes = new ArrayList<Crime>();
			Log.e(TAG, "Error loading crimes", e);
			Toast.makeText(mAppContext, "Error loading crimes.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Get a reference to the CrimeLab singleton.
	 * @param context This could be an Activity or another Context object like Service.
	 * @return the CrimeLab singleton.
	 */
	public static CrimeLab get(Context context)
	{
		if (sCrimeLab == null )  // Only the first time.
		{
			// Create one and only instance of the CrimeLab associated with the current global context. 
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
			if (crime.getId().equals(id))
			{
				return crime;
			}
		}
		return null;
	}
	
	/**
	 * Adds a new crime to the ArrayList that stores all the crimes.
	 * @param crime
	 */
	public void addCrime(Crime crime)
	{
		mCrimes.add(crime);
	}
	
	/**
	 * Delete a crime from the list. If the crime has a photo, delete it from disk.
	 * @param crime
	 */
	public void deleteCrime(Crime crime)
	{
		// If the crime has a photo, delete it from disk.
		if (crime.getPhoto() != null)
		{
			// Delete the photo file on disk.
			crime.getPhoto().deletePhoto(mAppContext);
		}
		
		// Delete the specified crime from the list.
		mCrimes.remove(crime);
	}
	
	public boolean loadCrimes()
	{
		
		
		
		// Load crimes from the file system.
		try
		{
			mCrimes = mSerializer.loadCrimes();
			Toast.makeText(mAppContext, "Crimes successfully loaded.",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		catch (Exception e)
		{
			mCrimes = new ArrayList<Crime>();
			Log.e(TAG, "Error loading crimes", e);
			Toast.makeText(mAppContext, "Error loading crimes.",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	/**
	 * Save the data of the crimes to a file on the device's file system.
	 */
	public boolean saveCrimes()
	{
		try
		{
			mSerializer.saveCrimes(mCrimes);
			//Log.d(TAG, "Crimes saved to file");
			Toast.makeText(mAppContext, "Crimes saved to file", Toast.LENGTH_SHORT).show();
			return true;
		}
		catch (Exception e)
		{
			//Log.e(TAG, "Error saving crimes: ", e);
			Toast.makeText(mAppContext, "Error saving crimes", Toast.LENGTH_SHORT).show();
			return false;
		}
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
