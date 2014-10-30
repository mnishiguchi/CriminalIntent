package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class C_ListFragment extends ListFragment
{
	/* INSTANCE VARIABLES */
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	private boolean mSubtitleVisible;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Notify the FragmentManager that this fragment needs to receive
		// options menu callbacks.
		setHasOptionsMenu(true);
		
		// Change what is displayed on the hosting activity's action bar.
		getActivity().setTitle(R.string.crimes_title);
		
		// Get the list of crimes via the CrimeLab singleton.
		mCrimes = CrimeLab.get(getActivity() ).getCrimes();
		
		// Set the list adapter.
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
		
		// Retain this fragment.
		setRetainInstance(true);
		
		// The action bar's subtitle initially hidden.
		mSubtitleVisible = false;
	}
	
	/* Note: ListFragments come with a default onCreateView() method.
		The default implementation of a ListFragment inflates a layout that
		defines a full screen ListView. */
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		// Let the super create the layout view.
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		// Set the subtitle if it was visible before the rotation.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			if (mSubtitleVisible)
			{
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
		}
		return v;
	}
	
	
	/* onResume() is the safest place to update a fragment's view. */
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Reload the list.
		( (CrimeAdapter) getListAdapter() ).notifyDataSetChanged();
	}
	
	/**
	 * Creates the options menu and populates it with the items defined
	 * in res/menu/fragment_crime_list.xml.
	 * The setHasOptionsMenu(boolean hasMenu) must be called in the onCreate
	 * of the fragment in order to notify the FragmentManager that this fragment
	 *  needs to receive options menu callbacks.
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		
		// Inflate the menu; this adds items to the action bar.
		inflater.inflate(R.menu.fragment_crime_list, menu);
		
		// Get a reference to the subtitle menu item.
		MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
		
		// Display the subtitle menu item's state based on mSubtitleVisible.
		if (mSubtitleVisible && showSubtitle != null)
		{
			showSubtitle.setTitle(R.string.hide_subtitle);
		}
	}
	
	/**
	 * Responds to menu selection.
	 */
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
		{
		 	switch (item.getItemId() )
		 	{
		 		case R.id.menu_item_new_crime:
		 			// Create a new Crime object and register it to the CrimeLab.
		 			Crime crime = new Crime();
		 			CrimeLab.get(getActivity() ).addCrime(crime) ;
		 			
		 			// Open an edit page.
		 			Intent i = new Intent(getActivity(), PagerActivity.class);
		 			i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId() );
		 			startActivityForResult(i, 0);
		 			
		 			return true;  // Indicate that no further processing is necessary.
		 			
		 		case R.id.menu_item_show_subtitle:
		 			// Set the action bar's subtitle, toggling "Show subtitle" & "Hide subtitle"
		 			if (getActivity().getActionBar().getSubtitle() == null)
		 			{
		 				getActivity().getActionBar().setSubtitle(R.string.subtitle);  // Show the subtitle
		 				mSubtitleVisible = true;
		 				item.setTitle(R.string.hide_subtitle);  // Say "Hide subtitle"
		 			}
		 			else
		 			{
		 				getActivity().getActionBar().setSubtitle(null);  // Hide the subtitle
		 				mSubtitleVisible = false;
		 				item.setTitle(R.string.show_subtitle);  // Say "Show subtitle"
		 			}
		 			return true;  // Indicate that no further processing is necessary.
		 			
		 		default:
		 			return super.onOptionsItemSelected(item);
		 	}
		}
		
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id)
	{
		// Get the selected item.
		Crime crime = ( (CrimeAdapter) getListAdapter() ).getItem(position);

		// Start the PagerActivity with the crime's UUID as an extra.
		Intent i = new Intent(getActivity(), PagerActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId() );  // UUID is a Serializable object.
		startActivity(i);
	}
	
	/** INNER CLASS
	 * - A custom ArrayAdapter designed to display Crime-specific list items.
	 */
	private class CrimeAdapter extends ArrayAdapter<Crime>
	{
		/** CONSTRUCTOR
		 * @param crimes - An ArrayList of Crime objects to be displayed in the ListView.
		 */
		public CrimeAdapter(ArrayList<Crime> crimes)
		{
			// The superclass constructor
			// (Set the args[1] to 0 when a pre-defined layout is not used.)
			super(getActivity(), 0, crimes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// If the convertView wasn't provided, inflate a new one. (Else recycle it)
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater()
												.inflate(R.layout.list_item_crime, null);
			}
			
			/* Configure the convertView for this particular Crime */
			
			// Get the crime object in question.
			Crime crime = getItem(position);
			
			TextView tvTitle = (TextView)
					convertView.findViewById(R.id.tv_list_item_crime_title);
			tvTitle.setText(crime.getTitle() );
			
			TextView tvDate = (TextView)
					convertView.findViewById(R.id.tv_list_item_crime_date);
			tvDate.setText(crime.getDate().toString() );
			
			CheckBox cb_Solved = (CheckBox)
					convertView.findViewById(R.id.cb_list_item_crime_solved);
			cb_Solved.setChecked(crime.isSolved() );
			
			return convertView;
		}
	}
}
