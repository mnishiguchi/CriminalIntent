package com.mnishiguchi.android.criminalintent;

import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CrimeFragment extends Fragment
{
	public final String TAG = "CriminalIntent";
	
	/* STATIC */
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	public static final String EXTRA_CRIME_ID = "com.mnishiguchi.android.criminalintent.crime_id";
	public static final String DIALOG_OPTION_DATETIME = "date";
	public static final int REQUEST_DATE = 0;
	
	/* INSTANCE VARIABLES */
	private Crime mCrime;  // Reference to the data stored in CrimeLab(model layer)
	
	private EditText mEtTitle;
	private Button mBtnDate;
	private CheckBox mCbSolved;
	
	/**
	 * Creates a new fragment instance and attaches the specified UUID as fragment's arguments.
	 * @param crimeId a UUID
	 * @return a new fragment instance with the specified UUID attached as its arguments.
	 */
	public static CrimeFragment newInstance(UUID crimeId)
	{
		// Prepare arguments.
		Bundle args = new Bundle();  // Contains key-value pairs.
		args.putSerializable(EXTRA_CRIME_ID, crimeId);
		
		// Creates a fragment instance and sets its arguments.
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Retrieve the arguments.
		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		
		// Fetch the Crime based on the crimeId
		mCrime = CrimeLab.get(getActivity() ).getCrime(crimeId);
		
		// Enable the options menu callback.
		setHasOptionsMenu(true);
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		// Get reference to the layout.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
		// Turn on the Up button.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			// If a parent activity is registered in the manifest file, enable the Up button.
			if (NavUtils.getParentActivityIntent(getActivity() ) != null)
			{
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		
		/* mEtTitle settings */ 
		
		mEtTitle = (EditText) v.findViewById(R.id.et_crime_title);
		mEtTitle.setText(mCrime.getTitle() );
		mEtTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence input, int start, int before, int count)
			{
				mCrime.setTitle(input.toString() );
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }  // Unused
			
			@Override
			public void afterTextChanged(Editable s) { }  // Unused
		} );
		
		/* mBtnDate settings */ 
		
		mBtnDate = (Button) v.findViewById(R.id.btn_crime_date);
		showUpdatedDate();
		mBtnDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				FragmentManager fm = getActivity().getSupportFragmentManager();
				
				// Create a DatePickerFragment with the crime's date as an argument.
				DateTimeOptionsFragment dialog =
						DateTimeOptionsFragment.newInstance(mCrime.getDate() );
				
				// Build a connection with the dialog to get the result returned later on.
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				
				// Show the DatePickerFragment.
				dialog.show(fm, DIALOG_OPTION_DATETIME);
			}
		} );
		
		/* mCbSolved settings */ 
		
		mCbSolved = (CheckBox) v.findViewById(R.id.cb_crime_solved);
		mCbSolved.setChecked(mCrime.isSolved() );
		mCbSolved.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				// Set the crime's solved property.
				mCrime.setSolved(isChecked);
			}
		} );
		
		// Return the layout.
		return v;
	}
	
	/**
	 * Set the latest updated date on the date button.
	 */
	private void showUpdatedDate()
	{
		mBtnDate.setText(mCrime.getDate().toString() );
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent i)
	{
		Log.i(TAG, getClass().getSimpleName() + ": entered onActivityResult()");
		Log.i(TAG, getClass().getSimpleName() + "requestCode == REQUEST_DATE : " +
					String.valueOf(requestCode == REQUEST_DATE) );
		
		if (resultCode != Activity.RESULT_OK) return;
		if (requestCode == REQUEST_DATE)
		{
			// Retrieve data from the passed-in Intent.
			Date date = (Date) i.getSerializableExtra(EXTRA_DATE);
			Log.i(TAG, getClass().getSimpleName() + "date: " + date.toString() );
			
			// Update the date in the model layer(CrimeLab)
			mCrime.setDate(date);
			
			// Set the updated date on the mBtnDate.
			showUpdatedDate();
		}
	}
	
	//@Override
	//public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { }
	
	/**
	 * Respond to menu selection.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	 	switch (item.getItemId() )
	 	{
	 		// Respond to the enabled Up icon as if it were an existing options menu item.
			case android.R.id.home:
				// If a parent activity is registered in the manifest file, move up the app hierarchy.
				if (NavUtils.getParentActivityName(getActivity() ) != null)
				{
					NavUtils.navigateUpFromSameTask(getActivity() );
				}
				return true;  // Indicate that no further processing is necessary.
		 			
			default:
				return super.onOptionsItemSelected(item);
	 	}
	}
	

}  // end class
