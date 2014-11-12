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
import android.widget.TimePicker;

public class DateTimePickerFragment extends DialogFragment
{
	public final String TAG = "CriminalIntent";
	
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	
	private Date mDate;
	
	// To remember the user's input.
	private int mYear, mMonth, mDay, mHour, mMin;

	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static DateTimePickerFragment newInstance(Date date)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putSerializable(CrimeFragment.EXTRA_DATE, date);
		
		// Create a new instance of DatePickerFragment.
		DateTimePickerFragment dialog = new DateTimePickerFragment();
		
		// Stash the date in DatePickerFragment's arguments bundle.
		dialog.setArguments(args);
		
		return dialog;
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
		Intent i = new Intent();
		i.putExtra(EXTRA_DATE, mDate);  // Date is a Serializable object.
		getTargetFragment().onActivityResult(CrimeFragment.REQUEST_DATE, resultCode, i);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		mDate = (Date)getArguments().getSerializable(EXTRA_DATE);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(mDate);
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMin = calendar.get(Calendar.MINUTE);

		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_datetime, null);

		DatePicker datePicker = (DatePicker)v.findViewById(R.id.dialog_datetime_datePicker);
		TimePicker timePicker = (TimePicker)v.findViewById(R.id.dialog_datetime_timePicker);

		datePicker.init(mYear, mMonth, mDay, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day)
			{
				// Remember updated values.
				mYear = year;
				mMonth = month;
				mDay = day;
				
				updateDate();
			}
		} );

		timePicker.setCurrentHour(mHour);
		timePicker.setCurrentMinute(mMin);
		timePicker.setIs24HourView(false);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hour, int min)
			{
				// Remember updated values.
				mHour = hour;
				mMin = min;
				
				updateDate();
			}
		} );

		return new AlertDialog.Builder(getActivity() )
				.setView(v)
				.setTitle(R.string.datetime_picker_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						sendResult(Activity.RESULT_OK);
					}
				} )
				.create();
	}

	/**
	 * Update mDate based on updated values that the user has inputed.
	 */
	public void updateDate()
	{
		// Translate year, month and day into a Date object.
		mDate = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMin).getTime();
		
		// Update arguments to preserve selected value on rotation.
		getArguments().putSerializable(CrimeFragment.EXTRA_DATE, mDate);
	}
}
