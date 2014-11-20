package com.mnishiguchi.android.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Creating a Photo class is useful when we need to display a caption or handling a touch event.
 */
public class Photo
{
	private static final String JSON_FILENAME ="filename";
	
	private String mFilename;
	
	/**
	 * Constructor to create a Photo from a specified filename.
	 */
	public Photo (String filename)
	{
		mFilename = filename;
	}
	
	/**
	 * Constructor to create a Photo from a specified JSON filename.
	 * Used when saving and loading its property of type Photo.
	 */
	public Photo (JSONObject json) throws JSONException
	{
		mFilename = json.getString(JSON_FILENAME);
	}
	
	/**
	 * Serialize this Photo into a JSON object.
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_FILENAME, mFilename);
		return json;
	}
	
	/**
	 * @return this Photo's filename
	 */
	public String getFilename()
	{
		return mFilename;
	}

}
