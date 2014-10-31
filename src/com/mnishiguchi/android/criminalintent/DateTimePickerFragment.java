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
	/* STATIC */
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	private int year, month, day, hour, min;

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
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		min = calendar.get(Calendar.MINUTE);

		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_datetime, null);

		DatePicker datePicker = (DatePicker)v.findViewById(R.id.dialog_datetime_datePicker);
		TimePicker timePicker = (TimePicker)v.findViewById(R.id.dialog_datetime_timePicker);

		datePicker.init(year, month, day, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day)
			{
				DateTimePickerFragment.this.year = year;
				DateTimePickerFragment.this.month = month;
				DateTimePickerFragment.this.day = day;
				updateDateTime();
			}
		} );

		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(min);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hour, int min)
			{
				DateTimePickerFragment.this.hour = hour;
				DateTimePickerFragment.this.min = min;
				updateDateTime();
			}
		});

		return new AlertDialog.Builder(getActivity())
				.setView(v)
				.setTitle(R.string.date_picker_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						sendResult(Activity.RESULT_OK);
					}
				} )
				.create();
	}

	public void updateDateTime()
	{
		mDate = new GregorianCalendar(year, month, day, hour, min).getTime();
		getArguments().putSerializable(EXTRA_DATE, mDate);
	}
}