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
	/* INSTANCE VARIABLES */
	private Date mDate;
	private int year, month, day, hour, min;
	
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
		
		// Create a Calendar to get integers for the year, month and day.
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(mDate);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		min = calendar.get(Calendar.MINUTE);
		
		// Inflate the dialog's layout defined in res/layout/dialog_time.xml.
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_time, null);
		
		// Initialize the TimePicker component.
		TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timePicker);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(min);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hour, int min)
			{
				TimePickerFragment.this.hour = hour;
				TimePickerFragment.this.min = min;
				updateDateTime();
			}
		} );

		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
						.setView(dialogView)
						.setTitle(R.string.date_picker_title)
						.setPositiveButton(
								android.R.string.ok, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										updateDateTime();
										sendResult(Activity.RESULT_OK);
									}
								} )
						.create();
	}
	
	public void updateDateTime()
	{
		// Translate year, month and day into a Date object.
		mDate = new GregorianCalendar(year, month, day, hour, min).getTime();
		
		// Update arguments to preserve selected value on rotation.
		getArguments().putSerializable(CrimeFragment.EXTRA_DATE, mDate);
	}
}
