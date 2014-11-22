package com.mnishiguchi.android.criminalintent;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
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
	
	private static final String DIALOG_DATETIME = "datetime";
	private static final String DIALOG_IMAGE = "image";
	private static final String DIALOG_DELETE = "delete";
	
	public static final int REQUEST_DATE = 0;
	public static final int REQUEST_PHOTO = 1;
	
	public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
			DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
	
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
	
	// Reference to CAB.
	private ActionMode mMode;
	
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
		Log.d(TAG, "Entered: onCreateView()");
		
		// Get reference to the layout.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
		// If a parent activity is registered in the manifest file, enable the Up button.
		if (NavUtils.getParentActivityIntent(getActivity() ) != null)
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
				mCrime.setTitle(input.toString() );
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
				String path = getActivity().getFileStreamPath(photo.getFilename()).getAbsolutePath();
				
				// Show an ImageFragment
				ImageFragment.newInstance(path).show(fm, DIALOG_IMAGE);
			}
		});

		// Long click => Contextual action for deleting photo.
		final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				// Remember reference to action mode.
				mMode = mode;
				
				// Inflate the menu using a special inflater defined in the ActionMode class.
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.crime_photo_context, menu);
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
					case R.id.menu_item_delete_photo: // Delete menu item.
						
						deletePhoto();
						//showToast("Delete Action clicked");

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
				// Required, but not used in this implementation.
			}
		};
		
		// Listen for long clicks. Start the CAB.
		mPhotoView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v)
			{
				boolean hasDrawable = (mPhotoView.getDrawable() != null);
				
				showToast("hasDrawable: " + hasDrawable);
				
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
				(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Camera.getNumberOfCameras() > 0);
		if (!hasCamera)
		{
			mBtnPhoto.setEnabled(false);
		}
		
		// Return the layout.
		return v;
	}
	
	/**
	 * Remove the Contextual Action Bar if any.
	 */
	public void finishCAB()
	{
		if (mMode != null) 
		 {
			mMode.finish();
			mMode = null;
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
		mBtnDate.setText(DATE_FORMAT.format(mCrime.getDate() ) );
	}
	
	/**
	 * Get the current crime's photo image from file storage and show it on the ImageView.
	 * Loading images in onStart() and them unloading in onStop is a good practice.
	 */
	private void showPhoto()
	{
		Photo photo = mCrime.getPhoto();
		BitmapDrawable bitmap = null;
		
		// Get a scaled bitmap.
		if (photo != null)
		{
			// Get the absolute path of the photo file on the filesystem. 
			String path = getActivity().getFileStreamPath(photo.getFilename())
					.getAbsolutePath(); // Convert the path to string.
			
			// Get a scaled bitmap drawable based on the data in this file.
			bitmap = PictureUtils.getScaledDrawable(getActivity(), path);
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
		
		showPhoto();
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
			
			// Set the updated date on the mBtnDate.
			showUpdatedDate();
		}
		
		// --- Retrieve filename of the photo just taken ---
		
		else if (requestCode == REQUEST_PHOTO)
		{
			String filename = resultData.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
			if (filename != null)
			{
				// Create a new Photo object based on the filename sent from CrimeCameraFragment.
				Photo photo = new Photo(filename);
				
				// Attach it to the crime.
				mCrime.setPhoto(photo);
				
				showPhoto();
			}
		}
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
		((PagerActivity)getActivity()).getPagerAdapter().notifyDataSetChanged();

		// Toast a message and finish this activity.
		showToast(crimeTitle + " has been deleted.");
		getActivity().finish();
	}
	
	/**
	 * Delete from disk and from CrimeLab  the photo of the currently shown Crime.
	 *  Show a toast message.
	 */
	private boolean deletePhoto()
	{
		if (null == mCrime.getPhoto())
		{
			showToast("Couldn't delete the photo");
			return false; // Fail.
		}
		
		// Get the image data file on disk.
		String path = getActivity().getFileStreamPath(mCrime.getPhoto().getFilename()).getAbsolutePath();
		File imageFile = new File(path);
		
		// Delete the file
		imageFile.delete();
		
		// Set the reference to null.
		mCrime.setPhoto(null);
		
		// Clean up the ImageView.
		PictureUtils.cleanImageView(mPhotoView);
		
		showToast("1 photo deleted");
		return true; // Success.
	}
	
	/**
	 * Show a confirmation message before actually deleting.
	 */
	public static class DeleteConfirmationFragment extends DialogFragment
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
