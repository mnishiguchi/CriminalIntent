package com.mnishiguchi.android.criminalintent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class CrimeJSONSerializer
{
	public final String TAG = "CriminalIntent : " + getClass().getSimpleName();
	
	private Context mContext;
	private String mFileName;
	
	/**
	 * Constructor.
	 */
	public CrimeJSONSerializer(Context context, String fileName)
	{
		mContext = context;
		mFileName = fileName;
	}
	
	/**
	 * Load crimes from the file system.
	 * @return a list of Crime objects.
	 */
	public ArrayList<Crime> loadCrimes() throws IOException, JSONException
	{
		ArrayList<Crime> crimes = new ArrayList<Crime>();
		BufferedReader reader = null;
		InputStream in = null;
		
		// Open the file , read it, and put it into a StringBuilder.
		try
		{
			in = mContext.openFileInput(mFileName);

			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			
			String line = null;
			while (null != (line = reader.readLine()))  // Read until the end of file.
			{
				// Line breaks are omitted and irrelevant.
				jsonString.append(line);
			}
			
			// Parse the JSON using JSONTokener.
			JSONArray jsonArray = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			
			// Build the ArrayList of crimes from JSONObjects.
			for (int i = 0, size = jsonArray.length();
					i < size; i++)
			{
				// Create a Crime object from each JSONObject, and add it to the list.
				crimes.add(new Crime(jsonArray.getJSONObject(i)));
			}
		}
		catch (FileNotFoundException e)
		{
			// Ignore this one; it happens when starting fresh.
		}
		finally
		{
			// Ensure that the underlying file handle is freed up even if an error occurs.
			if (reader != null)
			{
				reader.close();
			}
		}
		
		return crimes;  // a list of Crime objects.
	}
	
	public void saveCrimes(ArrayList<Crime> crimes) throws JSONException, IOException
	{
		// Build an array in JSON.
		JSONArray array = new JSONArray();
		for (Crime each : crimes)
		{
			// Convert each crime to JSON and put it in the array.
			array.put(each.toJSON());
		}
		
		// Write the file to disk.
		Writer writer = null;
		OutputStream out = null;
		try
		{
			out = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
			
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());  // Write the array as a compact JSON string.
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}
}
