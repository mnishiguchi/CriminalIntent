package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CrimeListFragment extends ListFragment
{
	private static final String TAG = "CrimeListFragment";
	
	private static final String DIALOG_DELETE = "delete";
	
	// Store reference to the current instance to this fragment.
	private static CrimeListFragment sCrimeListFragment;
	
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	// The state of the Action Bar's subtitle.
	private boolean mSubtitleVisible;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Store a reference to this instance.
		sCrimeListFragment = this;
		
		// Notify the FragmentManager that this fragment needs to receive options menu callbacks.
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
		// Inflate a custom layout with list & empty.
		View v = inflater.inflate(R.layout.fragment_crime_list, parent, false);
		
		// Set a listener to the emptylist's button.
		Button btnAddCrime = (Button) v.findViewById(R.id.btn_add_crime);
		btnAddCrime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{
				registerNewCrime();
			}
		});
		
		// Set the subtitle if it was visible before the rotation.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			if (mSubtitleVisible)
			{
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
		}
		
		// Get a ListView object by using android.R.id.list resource ID
		// instead of getListView() because the layout view is not created yet.
		ListView listView = (ListView)v.findViewById(android.R.id.list);
		
		// --- ContextMenu ---
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			// Use the floating context menus on Froyo and Gingerbread.
			registerForContextMenu(listView);
		}
		// --- Contexual Action Bar ---
		else
		{
			// Use the contextual action bar on Honeycomb and higher.
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			// Define responce to multi-choice.
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					// Inflate the menu using a special inflater defined in the ActionMode class.
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					// Required, but not used in this implementation.
					return false;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item)
				{
					switch (item.getItemId())
					{
						case R.id.menu_item_delete_crime:
							
							// Show Delete Confirmation dialog.
							DeleteConfirmationFragment.newInstance(getSelectedItems())
								.show(getActivity().getSupportFragmentManager(), DIALOG_DELETE);

							// Prepare the action mode to be destroyed.
							mode.finish();
							
							return true;
						
						default:
							return false;
					}
				}

				@Override
				public void onDestroyActionMode(ActionMode mode)
				{
					// Required, but not used in this implementation.
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode,
						int position, long id, boolean checked)
				{
					// Required, but not used in this implementation.
				}
			});
		}
		
		// Return the layout view.
		return v;
	}
	
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
	
	/* 
	 * Responds to menu selection.
	 */
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId() )
		{
			case R.id.menu_item_new_crime:
				
				registerNewCrime();
				return true;  // No further processing is necessary.
					
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
				return true;  // No further processing is necessary.
				
			case R.id.menu_item_sample_options:
				
				// Open AlertDialog for options.
				new SingleChoiceOptionsFragment().show(getFragmentManager(), null);
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/* Respond to a short click on a list item. */
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
	
	/**
	 * Create a new Crime object and register it to the CrimeLab.
	 * Proceed to the edit page.
	 */
	private void registerNewCrime()
	{
		Crime crime = new Crime();
		CrimeLab.get(getActivity() ).addCrime(crime) ;
			
		Intent i = new Intent(getActivity(), PagerActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId() );
		startActivityForResult(i, 0);
	}
	
	/* Respond to a long click on a list item. Open the context menu. 
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// Get the selected list position.
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		
		// Get the selected list item.
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		Crime selectedCrime = adapter.getItem(position);
		
		// Get the selected menu item and respond to it.
		switch (item.getItemId())
		{
			case R.id.menu_item_delete_crime:
				
				CrimeLab.get(getActivity()).deleteCrime(selectedCrime);
				adapter.notifyDataSetChanged();
				
				Toast.makeText(getActivity(),  "Selected item has been deleted.", Toast.LENGTH_SHORT).show();
				
				return true;
		}
		return super.onContextItemSelected(item);
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
			tvDate.setText(CrimeFragment.DATE_FORMAT.format(crime.getDate() ) );
			
			CheckBox cb_Solved = (CheckBox)
					convertView.findViewById(R.id.cb_list_item_crime_solved);
			cb_Solved.setChecked(crime.isSolved() );
			
			return convertView;
		}
	}
	
	/**
	 * @return an array of Crime objects that are selected.
	 */
	private Crime[] getSelectedItems()
	{
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		ArrayList<Crime> list = new ArrayList<Crime>(adapter.getCount());
		
		// Iterate over the list items.
		for (int index = adapter.getCount() - 1;
				index >= 0; index--)
		{
			// Check that  item is selected or not.
			if (getListView().isItemChecked(index))
			{
				// Add the selected items to list.
				list.add(adapter.getItem(index));
			} 
		}
		
		// Get the size of the result.
		int resultSize = list.size();
		
		// Convert to Integer array.
		Crime[] result = new Crime[resultSize];
		result = list.toArray(result);
		
		return result;
	}

	/**
	 * Delete selected list items and update the list view.
	 * @param selectedItems
	 * @return the number of items deleted.
	 */
	private int deleteSelectedItems(Crime[] selectedItems)
	{
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		CrimeLab crimeLab = CrimeLab.get(getActivity());
		int count = 0;
		
		// Delete the selected items from the CrimeLab's list.
		for (Crime each : selectedItems)
		{
			crimeLab.deleteCrime(each);
			count += 1;
		}
		
		// Update the ListView.
		adapter.notifyDataSetChanged();
		
		Toast.makeText(getActivity(), count + " item(s) deleted", Toast.LENGTH_SHORT).show();
		return count;
	}
	
	/**
	 * Show a confirmation message before actually deleting selected items.
	 */
	public static class DeleteConfirmationFragment extends DialogFragment
	{
		// Store the selected list item that was passed in.
		static Crime[] sSelectedItems;
		
		/**
		 * Create a new instance that is capable of deleting the specified list items.
		 */
		static DeleteConfirmationFragment newInstance(Crime[] selectedItems)
		{
			// Store the selected items so that we can refer to it later.
			sSelectedItems = selectedItems;
			
			// Create a fragment.
			DeleteConfirmationFragment fragment = new DeleteConfirmationFragment();
			fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
			
			return fragment;
		}
		
		/*
		 * Configure the dialog.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Define the response to buttons.
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
			{ 
				public void onClick(DialogInterface dialog, int which) 
				{ 
					switch (which) 
					{ 
						case DialogInterface.BUTTON_POSITIVE: 
							sCrimeListFragment.deleteSelectedItems(sSelectedItems);
							break; 
						case DialogInterface.BUTTON_NEGATIVE: 
							// do nothing 
							break; 
					} 
				}
			};
			
			// Create and return a dialog.
			return new AlertDialog.Builder(getActivity())
				.setTitle("Delete")
				.setMessage("Are you sure?")
				.setPositiveButton("Yes", listener)
				.setNegativeButton("Cancel", listener)
				.create();
		}
	}
	
	/**
	 * A dialog to show list of single-choice options.
	 */
	public static class SingleChoiceOptionsFragment extends DialogFragment
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			String[] options = {"Item1", "Item2", "Item3"};
			int initialSelection = 0;
					
			// Use the Builder class for convenient dialog construction
			return new AlertDialog.Builder(getActivity())
				.setTitle("Which one would you like?")
				.setPositiveButton("Set", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						// Do something.
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						// Do something.
					}
				})
				.setSingleChoiceItems(options, initialSelection ,
						new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
							case 1: // Do something.
								break;
							case 0: // Do something.
								break;
							default:
						}
					}
				})
				.create();
		}
	}
}
