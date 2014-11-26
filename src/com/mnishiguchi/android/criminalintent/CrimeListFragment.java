package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;

import com.mnishiguchi.android.criminalintent.CrimeFragment.DetailCallbacks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor") public class CrimeListFragment extends ListFragment
{
	private static final String TAG = "CriminalIntent.CrimeListFragment";
	
	private static final String DIALOG_DELETE = "delete";
	
	// Store reference to the current instance to this fragment.
	private static CrimeListFragment sCrimeListFragment;
	
	// Reference to the list of crimes stored in CrimeLab.
	private ArrayList<Crime> mCrimes;
	
	// The state of the Action Bar's subtitle.
	private boolean mSubtitleVisible;
	
	private int mPositionSelected;
	
	private ListCallbacks mCallbacks;
	
	/**
	 * Required interface for hosting activities.
	 */
	public interface ListCallbacks
	{
		void onCrimeSelected(Crime crime);
		void onActionMode();
		void onListItemsDeleted(Crime[] selectedItems);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		// Ensure that the hosting activity has implemented the callbacks
		try
		{
			mCallbacks = (ListCallbacks)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement CrimeListFragment.Callbacks");
		}
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallbacks = null;
	}
	
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		// Inflate a custom layout with list & empty.
		View v = inflater.inflate(R.layout.fragment_crime_list, parent, false);
		
		// Note:
		// Get a ListView object by using android.R.id.list resource ID
		// instead of getListView() because the layout view is not created yet.
		ListView listView = (ListView)v.findViewById(android.R.id.list);
		
		Button btnAddCrime = (Button) v.findViewById(R.id.btn_add_crime);
		btnAddCrime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{
				addNewCrime();
			}
		});
		
		if (mSubtitleVisible)
		{
			getActivity().getActionBar().setSubtitle(R.string.subtitle);
		}
		
		// --- Contexual Action Bar ---
		
		if (Utils.hasTwoPane(getActivity()))
		{
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		else
		{
			// Define responce to multi-choice.
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					// Inflate the menu using a special inflater defined in the ActionMode class.
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					
					// Call back.
					mCallbacks.onActionMode();
					return true;
				}
					
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					return false; // Return false if nothing is done
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

							mode.finish(); // Action picked, so close the CAB
							
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
					mode.setTitle(getListView().getCheckedItemCount() + " item(s) selected");
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId() )
		{
			// --- NEW ---
			
			case R.id.menu_item_new_crime:
				addNewCrime();
				return true;  // No further processing is necessary.
			
			// --- Show subtitle ---
			
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
				
			// --- Menu item => Open Dialog ---
				
			case R.id.menu_item_sample_options:
				// Open AlertDialog for options.
				new SingleChoiceOptionsFragment().show(getFragmentManager(), null);
			
			// --- Menu item => Open SubMenu ---
				
			case R.id.menu_item_sample_group:
				// Do something.
				return true;
			case R.id.submenu1:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				// Do something.
				return true;	
			case R.id.submenu2:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				// Do something.
				return true;
			case R.id.submenu3:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				// Do something.
				return true;
				
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
		
		// keep the selected position
		mPositionSelected = position;
		
		// Update the action bar title.
		getActivity().setTitle(crime.getTitle());
		
		// Call back.
		mCallbacks.onCrimeSelected(crime);
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
			tvDate.setText(crime.getDate().toString());
			
			CheckBox cb_Solved = (CheckBox)
					convertView.findViewById(R.id.cb_list_item_crime_solved);
			cb_Solved.setChecked(crime.isSolved() );
			
			return convertView;
		}
	}
	
	private void addNewCrime()
	{
		// Create and add a new Crime object to the CrimeLab's list.
		Crime crime = new Crime();
		CrimeLab.get(getActivity() ).addCrime(crime) ;
		
		// Update the listView.
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
		
		// For tablets only.
		if (Utils.hasTwoPane(getActivity()))
		{
			// Update the selection.
			setLastItemSelected();
			
			// Clear the action bar title.
			getActivity().setTitle("");
		}
		
		// callback
		mCallbacks.onCrimeSelected(crime);
	}
	
	public void updateUI()
	{		
		((CrimeAdapter) getListAdapter()).notifyDataSetChanged();
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
		
		// Call back.
		mCallbacks.onListItemsDeleted(selectedItems);
		
		Toast.makeText(getActivity(), count + " item(s) deleted", Toast.LENGTH_SHORT).show();
		return count;
	}
	
	/**
	 * Set the last list item selected.
	 */
	void setLastItemSelected()
	{
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		int lastIndex = adapter.getCount() - 1;
		getListView().setItemChecked(lastIndex, true);
	}
	
	/**
	 * Set the last list item selected.
	 */
	void clearListSelection()
	{
			CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
			getListView().clearChoices();
			adapter.notifyDataSetChanged();
	}
	
	/**
	 * Show a confirmation message before actually deleting selected items.
	 */
	static class DeleteConfirmationFragment extends DialogFragment
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
