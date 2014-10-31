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

public class TimePickerFragment extends DialogFragment
{
	/* STATIC */
	public static final String EXTRA_TIME = "com.mnishiguchi.android.criminalintent.time";
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	
	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static TimePickerFragment newInstance(Date date)
	{
		// Prepare arguments.
		//Bundle args = new Bundle();
		//args.putSerializable(EXTRA_TIME, date);
		
		// Create a new instance of DatePickerFragment.
		TimePickerFragment fragment = new TimePickerFragment();
		
		// Stash the date in DatePickerFragment's arguments bundle.
		//fragment.setArguments(args);
		
		return fragment;
	}
	
	/**
	 * Sends data to the target fragment.
	 * @param resultCode
	 */
	private void sendResult(int resultCode)
	{
		// Do nothing if there is no target fragment.
		//if (getTargetFragment() == null) return;
		
		// Send data to the target fragment.
		//Intent i = new Intent();
		//i.putExtra(EXTRA_TIME, mDate);  // Date is a Serializable object.
		//getTargetFragment().onActivityResult(CrimeFragment.REQUEST_DATE, resultCode, i);
	}
	
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Retrieve the arguments.
		mDate = (Date) getArguments().getSerializable(EXTRA_TIME);
		
		// Create a Calendar to get integers for the year, month and day.
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		// Inflate the DatePicker layout defined in res/layout/dialog_date.xml.
		View v = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_date, null);
		
		// Initialize the DatePicker component.
		DatePicker datePicker = (DatePicker) v.findViewById(R.id.dialog_date_datePicker);
		datePicker.init(year, month, day, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day)
			{
				// Translate year, month and day into a Date object.
				mDate = new GregorianCalendar(year, month, day).getTime();
				
				// Update arguments to preserve selected value on rotation.
				getArguments().putSerializable(EXTRA_TIME, mDate);
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
