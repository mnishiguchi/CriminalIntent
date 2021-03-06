package com.mnishiguchi.android.criminalintent;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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
	
	/**
	 * This is to check how many activities can respond to the passed-in intent.
	 * Run this check in onCreateView() to disable options that the device will not be able to respond to.
	 * If the OS cannot find a matching activity, then the app will crash.
	 * An Android device is guaranteed to have an email app and a contacts app of one kind or another.
	 */
	static boolean isIntentSafe(Activity activity, Intent i)
	{
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
		return (activities.size() > 0);
	}
}
