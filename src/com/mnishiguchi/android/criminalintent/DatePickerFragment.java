package com.mnishiguchi.android.criminalintent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
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
		// Retrieve the arguments.
		mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
		
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
				getArguments().putSerializable(EXTRA_DATE, mDate);
			}
		} );
		
		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
						.setView(v)
						.setTitle(R.string.date_picker_title)
						.setPositiveButton(android.R.string.ok, null)
						.create();
	}
	
}
