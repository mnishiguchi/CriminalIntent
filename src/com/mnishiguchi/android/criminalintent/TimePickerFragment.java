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
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
{
	private Date mDate;
	
	// To remember the user's input.
	private int mYear, mMonth, mDay, mHour, mMin;
	
	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static TimePickerFragment newInstance(Date date)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putSerializable(CrimeFragment.EXTRA_DATE, date);
		
		// Create a new instance of DatePickerFragment.
		TimePickerFragment dialog = new TimePickerFragment();
		
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
		i.putExtra(CrimeFragment.EXTRA_DATE, mDate);  // Date is a Serializable object.
		getTargetFragment().onActivityResult(CrimeFragment.REQUEST_DATE, resultCode, i);
	}
	
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Retrieve the arguments.
		mDate = (Date) getArguments().getSerializable(CrimeFragment.EXTRA_DATE);
		
		// Get initial integers for year, month, day, etc.
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(mDate);
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMin = calendar.get(Calendar.MINUTE);
		
		// Inflate the dialog's layout defined in res/layout/dialog_time.xml.
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_time, null);
		
		// Initialize the TimePicker component.
		TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.dialog_date_timePicker);
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

		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
				.setView(dialogView)
				.setTitle(R.string.time_picker_title)
				.setPositiveButton(
						android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								updateDate();
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
