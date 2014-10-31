package com.mnishiguchi.android.criminalintent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ListView;

public class DateTimeOptionsFragment extends DialogFragment
{
	public final String TAG = "CriminalIntent";
	
	/* STATIC */
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	public static final String DIALOG_DATE_PICKER = "date picker";
	public static final String DIALOG_TIME_PICKER = "time picker";
	public static int REQUEST_SET_DATE = 1;
	public static int REQUEST_SET_TIME = 2;
	
	/* INSTANCE VARIABLES */
	private Date mDate;
	private boolean mWantToSetDate = true;
	private boolean mWantToSetTime = false;
	private int year, month, day, hour, min;
	
	//private Context mAppContext = getActivity().getApplicationContext();
	/**
	 * Creates a new instance of DatePickerFragment and sets its arguments bundle.
	 * @param date
	 * @return a new instance of DatePickerFragment.
	 */
	public static DateTimeOptionsFragment newInstance(Date date)
	{
		// Prepare arguments.
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_DATE, date);
		
		// Create a new instance.
		DateTimeOptionsFragment fragment = new DateTimeOptionsFragment();
		
		// Stash the date in this fragment's arguments bundle.
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
		Intent i = new Intent();
		updateDate(year, month, day, hour, min);
		Log.i(TAG, getClass().getSimpleName() + ": sendResult() - " + year+"-"+month+"-"+day+"-"+hour+"-"+min);
		Log.i(TAG, getClass().getSimpleName() + ": sendResult() - " + mDate.toString());
		i.putExtra(EXTRA_DATE, mDate);  // Date is a Serializable object.
		getTargetFragment().onActivityResult(CrimeFragment.REQUEST_DATE, resultCode, i);
	}
	
	/**
	 * Update the date based on the user's input.
	 */
	private void updateDate(int year, int month, int day, int hour, int min)
	{
		mDate = new GregorianCalendar(year, month, day, hour, min).getTime();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}
	
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Retrieve the arguments.
		mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
		
		// Create a Calendar
		// Remember integers for the year, month, day, etc.
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mDate);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		min = calendar.get(Calendar.MINUTE);
		
		// Create a custom adapter for the dialog options.
		final ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.select_dialog_singlechoice);
		optionsAdapter.add("Set date");
		optionsAdapter.add("Set time");
		
		// Configure the AlertDialog and return it.
		return new AlertDialog.Builder(getActivity() )
				.setTitle(R.string.date_picker_title)
				.setPositiveButton("Test", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) { } // Do nothing.
						// Set the configuration in onStart()
				} )
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { }  // Do nothing.
				} )					
				.setSingleChoiceItems(optionsAdapter, 0,
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
							case 1:
								mWantToSetDate = false;
								mWantToSetTime = true;
								break;
							case 0: 
							default:
								mWantToSetDate = true;
								mWantToSetTime = false;
						}
					}
				} )
				.create();
	}
	
	@Override
	public void onStart()
	{
		// Here, dialog.show() is actually called on the underlying dialog.
		super.onStart();

		AlertDialog dialog = (AlertDialog)getDialog();
		if(dialog != null)
		{
			Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					if (wantToCloseDialog() )
					{
						// Send the date/time data to CrimeFragment.
						sendResult(Activity.RESULT_OK);
						
						// Close this dialog.
						setRetainInstance(false);
						dismiss();
					}
					
					if (mWantToSetDate)
					{
						// Start DatePicker.
						setDate();
					}
					if (mWantToSetTime)
					{
						// Start TimePicker.
					}
					
				}
		} );
	} }
	
	private void setDate()
	{
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		// Create a DatePickerFragment with the crime's date as an argument.
		DatePickerFragment dialog =
				DatePickerFragment.newInstance(year, month, day);
		
		// Build a connection with the dialog to get the result returned later on.
		dialog.setTargetFragment(DateTimeOptionsFragment.this, REQUEST_SET_DATE);
		
		// Show the DatePickerFragment.
		dialog.show(fm, DIALOG_DATE_PICKER);
	}
	
	/**
	 * @return true if both mWantToSetDate and mWantToSetTime are set to false.
	 */
	private boolean wantToCloseDialog()
	{
		if (!mWantToSetDate && !mWantToSetTime)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent i)
	{
		if (resultCode != Activity.RESULT_OK) return;
		if (requestCode == REQUEST_SET_DATE)
		{
			// Retrieve data from the passed-in Intent.
			year = i.getExtras().getInt(DatePickerFragment.EXTRA_YEAR);
			month = i.getExtras().getInt(DatePickerFragment.EXTRA_MONTH);
			day = i.getExtras().getInt(DatePickerFragment.EXTRA_DAY);
			
			this.mWantToSetDate = false;
		}
	}
}
