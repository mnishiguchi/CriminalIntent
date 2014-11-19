package com.mnishiguchi.android.criminalintent;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class Crime
{
	// FOR JSON
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_SOLVED = "solved";
	private static final String JSON_DATE = "date";
	private static final String JSON_PHOTO = "photo";
	
	// INSTANCE VARIABLES
	private UUID mId;
	private String mTitle	;
	private Date mDate;
	private boolean mSolved;
	private Photo mPhoto;
	
	/**
	 * Constructor. Create a default Crime object.
	 * Used when adding a new crime.
	 */
	public Crime()
	{
		// Generate unique identifier.
		mId = UUID.randomUUID();
		
		// Set mDate to the current date.
		mDate = new Date();
	}
	
	/**
	 * Constructor. Create a Crime object based on the passed-in JSONObject.
	 * Used when loading crimes from the file system.
	 * @param json a JSONObject that represents a crime.
	 * @throws JSONException
	 */
	public Crime(JSONObject json) throws JSONException
	{
		mId = UUID.fromString(json.getString(JSON_ID));
		if (json.has(JSON_TITLE))
		{
			mTitle = json.getString(JSON_TITLE);
		}
		mSolved = json.getBoolean(JSON_SOLVED);
		mDate = new Date(json.getLong(JSON_DATE));
		if (json.has(JSON_PHOTO))
		{
			mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
		}
	}
	
	/**
	 * Convert a Crime into a JSONObject.
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		
		// Add key-value pairs.
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_SOLVED, mSolved);
		json.put(JSON_DATE, mDate.getTime());
		if (mPhoto != null)
		{
			json.put(JSON_PHOTO, mPhoto.toJSON());
		}
		return json;
	}
	
	@Override
	public String toString()
	{
		return mTitle;
	}

	public String getTitle()
	{
		return mTitle;
	}
	
	public void setTitle(String title)
	{
		mTitle = title;
	}
	
	public UUID getId()
	{
		return mId;
	}

	public Date getDate()
	{
		return mDate;
	}

	public void setDate(Date date)
	{
		mDate = date;
	}

	public boolean isSolved()
	{
		return mSolved;
	}

	public void setSolved(boolean solved)
	{
		mSolved = solved;
	}

	public Photo getPhoto()
	{
		return mPhoto;
	}

	public void setPhoto(Photo photo)
	{
		mPhoto = photo;
	}

}
