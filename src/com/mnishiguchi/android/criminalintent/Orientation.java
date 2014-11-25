package com.mnishiguchi.android.criminalintent;

import android.content.Context;

/**
 * Represent a global storage for the device's orientation mode.
 */
class Orientation
{
	static final int NO_DATA = -1;
	static final int PORTRAIT_NORMAL =  1;
	static final int PORTRAIT_INVERTED =  2;
	static final int LANDSCAPE_NORMAL =  3;
	static final int LANDSCAPE_INVERTED =  4;
	
	// Store an instance of the Orientation.
	private static Orientation sOrientation;
	
	// Store orientation data.
	int mode = NO_DATA;
	
	/** 
	 * This is a private constructor to prevent anybody from 
	 * accidentally instantiate this class.
	 */
	private Orientation()
	{
		// Do nothing.
	}
	
	/**
	 * Get a reference to the Orientation singleton.
	 * @param context This could be an Activity or another Context object like Service.
	 * @return the Orientation singleton.
	 */
	static Orientation get(Context context)
	{
		if (sOrientation == null )  // Only the first time.
		{
			// Create one and only instance of the CrimeLab associated with the current global context. 
			sOrientation = new Orientation();
		}
		return sOrientation;
	}
}
