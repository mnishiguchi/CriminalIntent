package com.mnishiguchi.android.criminalintent;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Manages the AlertDialog that displays a DatePicker widget.
 */
public class DatePickerFragment extends DialogFragment
{
	/* STATIC */
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	
	/**
	 * Replaces the DatePickerFragment constructor.
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static DatePickerFragment newInstance(Date date)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_DATE, date);
		
		// Create a new instance of DatePickerFragment.
		DatePickerFragment fragment = new DatePickerFragment();
		
		// Stash the date in DatePickerFragment's arguments bundle.
		fragment.setArguments(args);
		
		return fragment;
	}
	
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Inflate the DatePicker layout defined in res/layout/dialog_date.xml.
		View v = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_date, null);
		
		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
						.setView(v)
						.setTitle(R.string.date_picker_title)
						.setPositiveButton(android.R.string.ok, null)
						.create();
	}
	
}
