package com.mnishiguchi.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Manages the AlertDialog that displays a DatePicker widget.
 */
public class DatePickerFragment extends DialogFragment
{
	/**
	 * Creates a new AlertDialog object and configure it.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Inflate the DatePicker layout defined in res/layout/dialog_date.xml.
		View v = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_date, null);
		
		// Configure it and return it.
		return new AlertDialog.Builder(getActivity() )
						.setView(v)
						.setTitle(R.string.date_picker_title)
						.setPositiveButton(android.R.string.ok, null)
						.create();
	}
	
}
