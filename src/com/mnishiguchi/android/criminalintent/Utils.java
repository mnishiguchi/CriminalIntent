package com.mnishiguchi.android.criminalintent;

import android.app.Activity;
import android.content.Context;

public class Utils
{
	/**
	 * Determine which interface was inflated, single-pane or two-pane.
	 * @return true if in the two-pane mode, else false.
	 */
	static boolean hasTwoPane(Context context)
	{
		return context.getResources().getBoolean(R.bool.has_two_panes);
	}
}
