package com.mnishiguchi.android.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CrimeFragment extends Fragment
{
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
		mCrime = new Crime();
	}
	
	/** Must be public because it'll be called by the hosting Activity. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		// Get reference to the UI components.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		mEtTitle = (EditText) v.findViewById(R.id.et_crime_title);
		mBtnDate = (Button) v.findViewById(R.id.btn_crime_date);
		mCbSolved = (CheckBox) v.findViewById(R.id.cb_crime_solved);
		
		// Listen for EditText changes.
		mEtTitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence input, int start, int before, int count)
			{
				mCrime.setTitle(input.toString() );
			}
			@Override  // Unused
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override //  Unused
			public void afterTextChanged(Editable s) { }
		} );
		
		// Set the date on the mBtnDate.
		mBtnDate.setText(mCrime.getDate().toString() );
		mBtnDate.setEnabled(false);
		
		// Listen for CheckBox changes.
		mCbSolved.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				// Set the crime's solved property.
				mCrime.setSolved(isChecked);
			}
		} );

		return v;
	}

}  // end class
