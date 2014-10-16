package com.mnishiguchi.android.criminalintent;

import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
	/* STATIC */
	public static final String EXTRA_CRIME_ID = "com.mnishiguchi.android.criminalintent.crime_id";
	public static final String DIALOG_DATE = "date";
	
	/* INSTANCE VARIABLES */
	private Crime mCrime;
	
	private EditText mEtTitle;
	private Button mBtnDate;
	private CheckBox mCbSolved;
	
	/** Must be public because it'll be called by the hosting Activity. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Retrieve the arguments passed by the CrimeActivity.
		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		
		// Fetch the Crime based on the crimeId
		mCrime = CrimeLab.get(getActivity() ).getCrime(crimeId);
	}
	
	/** Must be public because it'll be called by the hosting Activity. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		// Get reference to the layout.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
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
		mBtnDate.setText(mCrime.getDate().toString() );
		mBtnDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				// Show a DatePickerFragment.
				FragmentManager fm = getActivity().getSupportFragmentManager();
				DatePickerFragment dialog = new DatePickerFragment();
				dialog.show(fm, DIALOG_DATE);
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

}  // end class
