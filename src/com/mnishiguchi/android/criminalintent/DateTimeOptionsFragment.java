package com.mnishiguchi.android.criminalintent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.ArrayAdapter;

public class DateTimeOptionsFragment extends DialogFragment
{
	public final String TAG = "CriminalIntent";
	
	/* STATIC */
	public static final String DIALOG_TIME_OR_DATE = "time or date";
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	private boolean mSelectedTime = false;
	
	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static DateTimeOptionsFragment newInstance(Date date)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putSerializable(CrimeFragment.EXTRA_DATE, date);
	
		// Create a new instance.
		DateTimeOptionsFragment fragment = new DateTimeOptionsFragment();
		
		// Stash the date in this fragment's arguments bundle.
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
		mDate = (Date) getArguments().getSerializable(CrimeFragment.EXTRA_DATE);
	
		// Create a custom adapter for the dialog options.
		final ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.select_dialog_singlechoice);
		optionsAdapter.add("Set date");
		optionsAdapter.add("Set time");
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(mDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		String dateString = year + "-" + month+1 + "-" + day;
		String timeString = hour + ":" + min;
		String[] options = {dateString, timeString};
		
		// Configure the AlertDialog and return it.
		return new AlertDialog.Builder(getActivity() )
				.setTitle(mDate.toString() )
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { }  // Do nothing.
				} )
				//.setSingleChoiceItems(optionsAdapter, 0,
				.setItems(options, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
							case 0: mSelectedTime = false;
								break;
							case 1: mSelectedTime = true;
								break;
						}
						setDateOrTime();
					}
				} )
				.create();
	}
	/**
	 * Show a datePicker of timePicker based on the user's selection.
	 */
	private void setDateOrTime()
	{
		FragmentManager fm = getActivity().getSupportFragmentManager();
		DialogFragment dialog;

		if (mSelectedTime)
		{
			dialog = TimePickerFragment.newInstance(mDate);
		}
		else
		{
			dialog = DatePickerFragment.newInstance(mDate);
		}

		dialog.setTargetFragment(getTargetFragment(), getTargetRequestCode() );
		dialog.show(fm, DIALOG_TIME_OR_DATE);  
	}
	
}
