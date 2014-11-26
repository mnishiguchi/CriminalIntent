package com.mnishiguchi.android.criminalintent;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mnishiguchi.android.criminalintent.CrimeListFragment.ListCallbacks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CrimeFragment extends Fragment
{
	private static final String TAG = "CriminalIntent.CrimeFragment";
	
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	public static final String EXTRA_CRIME_ID = "com.mnishiguchi.android.criminalintent.crime_id";
	public static final String EXTRA_PHOTO_ORIENTATION = "com.mnishiguchi.android.android.criminalintent.photo_orientation";
	
	private static final String DIALOG_DATETIME = "datetime";
	private static final String DIALOG_IMAGE = "image";
	private static final String DIALOG_DELETE = "delete";
	
	public static final int REQUEST_DATE = 0;
	public static final int REQUEST_PHOTO = 1;
	public static final int REQUEST_CONTACT = 2;
	
	// Store reference to an instance of this fragment that is currently working..
	private static CrimeFragment sCrimeFragment;
		
	// Reference to a Crime object stored in CrimeLab (model layer)
	private Crime mCrime;
	
	// UI components
	private EditText mEtTitle;
	private Button mBtnDate;
	private CheckBox mCheckSolved;
	private ImageView mPhotoView;
	private ImageButton mBtnPhoto;
	private Button mSuspectButton;
	
	// Reference to CAB.
	private ActionMode mActionMode;
	
	private DetailCallbacks mCallbacks;
	
	/**
	 * Required interface for hosting activities.
	 */
	public interface DetailCallbacks
	{
		void onCrimeAdded(Crime crime);
		void onCrimeUpdated(Crime crime);
		void onCrimeDeleted(Crime crime);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		// Ensure that the hosting activity has implemented the callbacks
		try
		{
			mCallbacks = (DetailCallbacks)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement CrimeFragment.Callbacks");
		}
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallbacks = null;
	}
	
	/**
	 * Creates a new fragment instance and attaches the specified UUID as fragment's arguments.
	 * @param crimeId a UUID
	 * @return a new fragment instance with the specified UUID attached as its arguments.
	 */
	public static CrimeFragment newInstance(UUID crimeId)
	{
		// Prepare arguments.
		Bundle args = new Bundle();  // Contains key-value pairs.
		args.putSerializable(EXTRA_CRIME_ID, crimeId);
		
		// Creates a fragment instance and sets its arguments.
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate()");
		
		// Store a reference to this instance.
		sCrimeFragment = this;
		
		// Retrieve the arguments.
		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		
		// Fetch the Crime based on the crimeId
		mCrime = CrimeLab.get(getActivity() ).getCrime(crimeId);
		
		// Enable the options menu callback.
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView()");
		
		// Get reference to the layout.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
		// If a parent activity is registered in the manifest file, enable the Up button.
		if (!Utils.hasTwoPane(getActivity()) && NavUtils.getParentActivityIntent(getActivity() ) != null)
		{
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		else
		{
			Log.d(TAG, "Couldn't enable the Up button");
		}
		
		// --- Title EditText ---
		
		mEtTitle = (EditText) v.findViewById(R.id.et_crime_title);
		mEtTitle.setText(mCrime.getTitle() );
		mEtTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence input, int start, int before, int count)
			{
				mCrime.setTitle(input.toString());
				
				// Update the action bar title.
				getActivity().setTitle(mCrime.getTitle());
				
				// Notify it.
				mCallbacks.onCrimeUpdated(mCrime);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// Required, but not used in this implementation.
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				// Required, but not used in this implementation.
			}
		});
		
		// --- Date Button --- 
		
		mBtnDate = (Button) v.findViewById(R.id.btn_crime_date);
		showUpdatedDate();
		mBtnDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				FragmentManager fm = getActivity().getSupportFragmentManager();
				DialogFragment dialog;
				
				// Create a DatePickerFragment with the crime's date as an argument.
				boolean hasDateTimePicker = getResources().getBoolean(R.bool.has_datetime_picker);
				if (hasDateTimePicker)
				{
					dialog = DateTimePickerFragment.newInstance(mCrime.getDate() );
				}
				else
				{
					dialog = 	DateTimeOptionsFragment.newInstance(mCrime.getDate() );
				}
				
				// Build a connection with the dialog to get the result returned later on.
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				
				// Show the DatePickerFragment.
				dialog.show(fm, DIALOG_DATETIME);
			}
		} );
		
		// --- Solved CheckBox --- 
		
		mCheckSolved = (CheckBox) v.findViewById(R.id.cb_crime_solved);
		mCheckSolved.setChecked(mCrime.isSolved() );
		mCheckSolved.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				// Set the crime's solved property.
				mCrime.setSolved(isChecked);
				mCallbacks.onCrimeUpdated(mCrime);
			}
		} );
		
		// --- Photo View ---
		
		// Short click => Show the photo in full size.
		mPhotoView = (ImageView)v.findViewById(R.id.crime_imageView);
		mPhotoView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Photo photo = mCrime.getPhoto();
				if (null == photo) return;
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				
				// Get the absolute path for this crime's photo.
				String path = photo.getAbsolutePath(getActivity());
				
				// Get the orientation of this crime's photo.
				int orientation = photo.getOrientation();
				
				// Show an ImageFragment
				ImageFragment.newInstance(path, orientation).show(fm, DIALOG_IMAGE);
			}
		});

		// Long click => Contextual action for deleting photo.
		final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				// Remember reference to action mode.
				mActionMode = mode;
				
				// Inflate the menu using a special inflater defined in the ActionMode class.
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.crime_photo_context, menu);
				return true;
			}
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				mode.setTitle("Photo Checked");
				return false;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				switch (item.getItemId())
				{
					case R.id.menu_item_delete_photo: // Delete menu item.
						
						deletePhoto();

						// Prepare the action mode to be destroyed.
						mode.finish(); // Action picked, so close the CAB
						return true;
					
					default:
						return false;
				}
			}

			@Override
			public void onDestroyActionMode(ActionMode mode)
			{
				// Set it to null because we exited the action mode.
				mActionMode = null;
			}
		};
		
		// Listen for long clicks. Start the CAB.
		mPhotoView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v)
			{
				// Ignore the long click if already in the ActionMode.
				if (mActionMode != null) return false;
				
				// Check if a photo is set on the ImageView.
				boolean hasDrawable = (mPhotoView.getDrawable() != null);
				if (hasDrawable)
				{
					// Show the Contexual Action Bar.
					getActivity().startActionMode(actionModeCallback);
				}
				
				return true; // Long click was consumed.
			}
		});
		
		// --- Photo Button ---
		
		mBtnPhoto = (ImageButton)v.findViewById(R.id.crime_imageButton);
		mBtnPhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				// Start the camera, requesting the photo's filename, if one taken.
				Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
				startActivityForResult(i, REQUEST_PHOTO);
			}
		});
		
		// --- Checking For Camera Availability ---
		
		// if camera is not available, disable camera functionality.
		
		PackageManager pm = getActivity().getPackageManager();
		boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
				pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
				Camera.getNumberOfCameras() > 0;
		if (!hasCamera)
		{
			mBtnPhoto.setEnabled(false);
		}
		
		// --- Report button ---
		
		// Show a list of reporting apps.
		final Button reportButton = (Button)v.findViewById(R.id.crime_reportButton);
		reportButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
				i.putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject);
				
				// Set the chooser so that the user can choose every time they push this button.
				i = Intent.createChooser(i,  getString(R.string.send_report));
				startActivity(i);
			}
		});
		
		// --- Suspect button ---
		
		// Show a list of contacts.
		mSuspectButton = (Button)v.findViewById(R.id.crime_suspectButton);
		mSuspectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(i, REQUEST_CONTACT);
			}
		});
		
		// Set the suspect's name on the button.
		if (mCrime.getSuspect() != null)
		{
			mSuspectButton.setText(mCrime.getSuspect());
		}
		
		// Return the layout.
		return v;
	}
	
	/**
	 * Remove the Contextual Action Bar if any.
	 */
	public void finishCAB()
	{
		if (mActionMode != null) 
		 {
			mActionMode.finish();
			mActionMode = null;
		 }
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (!isVisibleToUser)
		{
			finishCAB();
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause()");
		
		CrimeLab.get(getActivity()).saveCrimes();
	}
	
	/**
	 * Set the latest updated date on the date button.
	 */
	private void showUpdatedDate()
	{
		//mBtnDate.setText(DATE_FORMAT.format(mCrime.getDate() ) );
		mBtnDate.setText(mCrime.getDate().toString());
	}
	
	/**
	 * Get the current crime's photo image from file storage and show it on the ImageView.
	 * Loading images in onStart() and them unloading in onStop is a good practice.
	 */
	private void showThumbnail()
	{
		// Ensure that this Crime has a photo.
		if (null == mCrime.getPhoto())
		{
			mCrime.setPhoto(null);
			showToast("No photo found for this crime");
			return ; // Fail.
		}
		
		// Get a scaled bitmap.
		Photo photo = mCrime.getPhoto();
		BitmapDrawable bitmap = photo.loadBitmapDrawable(getActivity());
		
		// Check the orientation. If necessary, change the bitmap orientation.
		int orientation = Orientation.get(getActivity()).mode;
		
		if (orientation == Orientation.PORTRAIT_INVERTED ||
				orientation == Orientation.PORTRAIT_NORMAL)
		{
			bitmap = PictureUtils.getPortraitDrawable(mPhotoView, bitmap);
		}

		// Set the image on the ImageView.
		mPhotoView.setImageDrawable(bitmap);
	}
	
	/*
	 * Have the photo ready as soon as this Fragment's view becomes visible to the user.
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		
		showThumbnail();
	}
	
	/*
	 * Unload the photo as soon as this Fragment's view becomes invisible to the user.
	 */
	@Override
	public void onStop()
	{
		super.onStop();
		
		PictureUtils.cleanImageView(mPhotoView);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData)
	{
		if (resultCode != Activity.RESULT_OK) return;
		
		// --- Retrieve updated date ---
		
		if (requestCode == REQUEST_DATE)
		{
			// Retrieve data from the passed-in Intent.
			Date date = (Date) resultData.getSerializableExtra(EXTRA_DATE);
			
			// Update the date in the model layer(CrimeLab)
			mCrime.setDate(date);
			mCallbacks.onCrimeUpdated(mCrime);
			
			// Set the updated date on the mBtnDate.
			showUpdatedDate();
		}
		
		// --- Retrieve filename of the photo just taken ---
		
		else if (requestCode == REQUEST_PHOTO)
		{
			// Retrieve data from the passed-in Intent.
			String filename = resultData.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
			int orientation = resultData.getIntExtra(CrimeCameraFragment.EXTRA_PHOTO_ORIENTATION, Orientation.NO_DATA);
			
			// Delete the old photo, if any.
			if (mCrime.getPhoto() != null)
			{
				deletePhoto();
			}
			
			if (filename != null)
			{
				// Create a new Photo object based on the filename sent from CrimeCameraFragment.
				Photo photo = new Photo(filename, orientation);
				
				// Attach it to the crime.
				mCrime.setPhoto(photo);
				
				// Notify it.
				mCallbacks.onCrimeUpdated(mCrime);
				
				showThumbnail();
			}
		}
		
		// --- Retrieve contact name ---
		
		else if (requestCode == REQUEST_CONTACT)
		{
			// This URI is a locator that points at the single contact the user picked.
			Uri contactUri = resultData.getData();
			
			// Specify which field you want your query to return values for.
			String[] queryFields = new String[] {
					ContactsContract.Contacts.DISPLAY_NAME
			};
			
			// Perform your query.
			Cursor cursor = getActivity().getContentResolver()
					.query(contactUri, queryFields, null, null, null);
			
			// Double-check that you actually got results
			if (cursor.getCount() == 0)
			{
				cursor.close();
				return;
			}
			
			// Pull out the first column of the first row of data.
			cursor.moveToFirst(); // first row
			String suspect = cursor.getString(0);
			mCrime.setSuspect(suspect);
			mCallbacks.onCrimeUpdated(mCrime);
			
			// Set the suspect's name on the button.
			mSuspectButton.setText(suspect);
			
			cursor.close();
		}
	}
	
	/**
	 * This is to check how many activities can respond to the passed-in intent.
	 * Run this check in onCreateView() to disable options that the device will not be able to respond to.
	 * If the OS cannot find a matching activity, then the app will crash.
	 * An Android device is guaranteed to have an email app and a contacts app of one kind or another.
	 */
	@SuppressWarnings("unused")
	private boolean isIntentSafe(Intent i)
	{
		PackageManager pm = getActivity().getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
		return (activities.size() > 0);
	}
	
	/**
	 * Creates the options menu and populates it with the items defined
	 * in res/menu/fragment_crime.xml.
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		
		// Inflate the menu; this adds items to the action bar.
		inflater.inflate(R.menu.fragment_crime, menu);
	}
	
	/**
	 * Respond to menu selection.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	 	// Check the selected menu item and respond to it.
		switch (item.getItemId() )
	 	{
	 		// Respond to the enabled Up icon as if it were an existing options menu item.
			case android.R.id.home:
				
				// If a parent activity is registered in the manifest file, move up the app hierarchy.
				if (NavUtils.getParentActivityName(getActivity() ) != null)
				{
					NavUtils.navigateUpFromSameTask(getActivity() );
				}
				return true;  // Indicate that no further processing is necessary.

			case R.id.menu_item_delete_crime:
				
				// Show the delete dialog.
				DeleteConfirmationFragment.toDeleteCrime(mCrime)
					.show(getFragmentManager(), DIALOG_DELETE);

			default:
				return super.onOptionsItemSelected(item);
	 	}
	}
	
	private void setActionBarTitle(String title)
	{
		// Update the action bar title.
		getActivity().setTitle(title);
	}
	/**
	 * Delete the currently shown Crime from CrimeLab's list. Update the Pager.
	 * Finish this fragment. Show a toast message.
	 */
	private void deleteCrime(Crime crime)
	{
		// Get the crime title.
		String crimeTitle = (crime.getTitle() == null || crime.getTitle().equals("")) ?
				"(No title)" : crime.getTitle();
		
		// Delete the crime.
		CrimeLab.get(getActivity()).deleteCrime(crime);
		
		// Update the pager adapter.
		if (Utils.hasTwoPane(getActivity())) // Tablet
		{
			mCallbacks.onCrimeDeleted(mCrime);
		}
		else // Phone
		{
			((PagerActivity)getActivity()).getPagerAdapter().notifyDataSetChanged();
			// Toast a message and finish this activity.
			showToast(crimeTitle + " has been deleted.");
			getActivity().finish();
		}
	}
	
	/**
	 * Delete from disk and from CrimeLab  the photo of the currently shown Crime.
	 *  Show a toast message.
	 */
	private void deletePhoto()
	{
		if (null == mCrime.getPhoto())
		{
			showToast("No photo found");
			return ; // Fail.
		}
		
		// Clean up the ImageView.
		PictureUtils.cleanImageView(mPhotoView);
		
		// Delete the image data file on disk.
		boolean success = mCrime.getPhoto().deletePhoto(getActivity());
		if (success)
		{
			showToast("1 photo deleted");
			
			// Set the reference to null.
			mCrime.setPhoto(null);
		}
		else
		{
			showToast("Couldn't delete the photo");
		}
	}
	
	/**
	 * Create a string for the reporting purpose.
	 */
	private String getCrimeReport()
	{
		String solvedString = null;
		
		if (mCrime.isSolved())
		{
			solvedString = getString(R.string.crime_report_solved);
		}
		else
		{
			solvedString = getString(R.string.crime_report_unsolved);
		}
		
		String dateString = formatDateForReport(mCrime.getDate());
		
		String suspect = mCrime.getSuspect();
		if (null == suspect)
		{
			suspect = getString(R.string.crime_report_no_suspect); // Interpolaton
		}
		else
		{
			suspect = getString(R.string.crime_report_suspect, suspect);
		}

		String report = getString(R.string.crime_report,
				mCrime.getTitle(), dateString, solvedString, suspect);  // Interpolaton
		
		return report;
	}

	private String formatDateForReport(Date date)
	{
		String dateFormat = "EEE, MMM dd";
		return (String) DateFormat.format(dateFormat, date).toString();
	}
	
	/**
	 * Show a confirmation message before actually deleting.
	 */
	static class DeleteConfirmationFragment extends DialogFragment
	{
		// Store the Crime that was passed in.
		static Crime sCrime;
	
		/**
		 * Create a new instance that is capable of deleting the specified list items.
		 */
		static DeleteConfirmationFragment toDeleteCrime(Crime crime)
		{
			// Store the Crime so that we can refer to it later.
			sCrime = crime;
			
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

							sCrimeFragment.deleteCrime(sCrime);
							Log.d(TAG, "wantsToDeleteCrime");
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
	 * Show a toast message.
	 */
	private void showToast(String msg)
	{
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
}  // end class
