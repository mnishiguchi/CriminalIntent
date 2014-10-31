package com.mnishiguchi.android.criminalintent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * Manages the AlertDialog that displays a DatePicker widget.
 */
public class DatePickerFragment extends DialogFragment
{
	/* STATIC */
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	public static final String EXTRA_YEAR = "com.mnishiguchi.android.criminalintent.year";
	public static final String EXTRA_MONTH = "com.mnishiguchi.android.criminalintent.month";
	public static final String EXTRA_DAY = "com.mnishiguchi.android.criminalintent.day";
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	private int year, month, day;
	
	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static DatePickerFragment newInstance(int year, int month, int day)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putInt(EXTRA_YEAR, year);
		args.putInt(EXTRA_MONTH, month);
		args.putInt(EXTRA_DAY, day);
		
		// Create a new instance of DatePickerFragment.
		DatePickerFragment fragment = new DatePickerFragment();
		
		// Stash the date in DatePickerFragment's arguments bundle.
		fragment.setArguments(args);
		
		return fragment;
	}
	
	/**
	 * Sends data to the target fragment.
	 * @param resultCode
	 */
	private void sendResult(int resultCode)
	{
		// Do nothing if there is no target fragment.
		if (getTargetFragment() == null) return;
		
		// Send data to the target fragment.
		Intent resultIntent = new Intent();
		resultIntent.putExtra(EXTRA_YEAR, year);
		resultIntent.putExtra(EXTRA_MONTH, month);
		resultIntent.putExtra(EXTRA_DAY, day);
		
		getTargetFragment().onActivityResult(
				DateTimeOptionsFragment.REQUEST_SET_DATE, resultCode, resultIntent);
	}
	
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Retrieve the arguments.
		year =  getArguments().getInt(EXTRA_YEAR);
		month =  getArguments().getInt(EXTRA_MONTH);
		day =  getArguments().getInt(EXTRA_DAY);
		
		// Inflate the DatePicker layout defined in res/layout/dialog_date.xml.
		View v = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_date, null);
		
		// Initialize the DatePicker component.
		DatePicker datePicker = (DatePicker) v.findViewById(R.id.dialog_date_datePicker);
		datePicker.init(year, month, day, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day)
			{
				// Update arguments to preserve selected value on rotation.
				getArguments().putInt(EXTRA_YEAR, year);
				getArguments().putInt(EXTRA_MONTH, month);
				getArguments().putInt(EXTRA_DAY, day);
			}
		} );

		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
						.setView(v)
						.setTitle(R.string.date_picker_title)
						.setPositiveButton(
								android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										sendResult(Activity.RESULT_OK);
									}
								} )
						.create();
	}
	
}
