package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class C_ListFragment extends ListFragment
{
	/* STATIC */
	private static final String TAG = "CListFragment";
	
	/* INSTANCE VARIABLES */
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Change what is displayed on the hosting activity's action bar.
		getActivity().setTitle(R.string.crimes_title);
		
		// Get the CrimeLab singleton and then get the list of crimes.
		mCrimes = CrimeLab.get(getActivity() ).getCrimes();
		
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
	}
	
	/* Note: ListFragments come with a default onCreateView() method.
		The default implementation of a ListFragment inflates a layout that
		defines a full screen ListView. */
	
	/* onResume() is the safestplace to take action to update a fragment's view. */
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Reload the list.
		( (CrimeAdapter) getListAdapter() ).notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id)
	{
		Crime crime = ( (CrimeAdapter) getListAdapter() ).getItem(position);
		
		Toast.makeText(getActivity(), crime.getTitle() + " was clicked", Toast.LENGTH_SHORT).show();

		// Start the CrimeActivity.
		Intent i = new Intent(getActivity(), CrimeActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId() );  // UUID is a Serializable object.
		startActivity(i);
	}
	
	/**
	 * A custom ArrayAdapter designed to display Crime-specific list items.
	 */
	private class CrimeAdapter extends ArrayAdapter<Crime>
	{
		/** CONSTRUCTOR
		 * @param crimes - An ArrayList of Crime objects to be displayed in the ListView.
		 */
		public CrimeAdapter(ArrayList<Crime> crimes)
		{
			// The superclass constructor
			// (Set the arg[1] to 0 when a pre-defined layout is not used.)
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
