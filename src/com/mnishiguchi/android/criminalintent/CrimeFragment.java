package com.mnishiguchi.android.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class CrimeFragment extends Fragment
{
	private Crime mCrime;
	private EditText mEtTitle;
	
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
		mEtTitle = (EditText) v.findViewById(R.id.crime_title);
		
		// Set an event handler.
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
		return v;
	}

}  // end class
