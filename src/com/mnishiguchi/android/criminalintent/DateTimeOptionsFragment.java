package com.mnishiguchi.android.criminalintent;

import java.text.SimpleDateFormat;
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

		// Option list items.
		String[] options = { "Set Date", "Set Time"};
		
		// Configure the AlertDialog and return it.
		return new AlertDialog.Builder(getActivity() )
				.setTitle("Currently: " + CrimeFragment.DATE_FORMAT.format(mDate) )
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { }  // Do nothing.
				} )
				.setItems(options, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						FragmentManager fm = getActivity().getSupportFragmentManager();
						DialogFragment picker = null;
						switch (which)
						{
							case 0: picker = DatePickerFragment.newInstance(mDate);
								break;
							case 1: picker = TimePickerFragment.newInstance(mDate);
								break;
						}
						picker.setTargetFragment(getTargetFragment(), getTargetRequestCode() );
						picker.show(fm, DIALOG_TIME_OR_DATE);  
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
