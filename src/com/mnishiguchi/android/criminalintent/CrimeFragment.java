package com.mnishiguchi.android.criminalintent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
	private static final String TAG = "tag_CrimeFragment";
	
	public static final String EXTRA_DATE = "com.mnishiguchi.android.criminalintent.date";
	public static final String EXTRA_CRIME_ID = "com.mnishiguchi.android.criminalintent.crime_id";
	
	private static final String DIALOG_DATETIME = "datetime";
	private static final String DIALOG_IMAGE = "image";
	
	public static final int REQUEST_DATE = 0;
	public static final int REQUEST_PHOTO = 1;
	
	public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
			DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
	
	// Reference to a Crime object stored in CrimeLab (model layer)
	private Crime mCrime;
	
	// UI components
	private EditText mEtTitle;
	private Button mBtnDate;
	private CheckBox mCheckSolved;
	private ImageView mPhotoView;
	private ImageButton mBtnPhoto;
	
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
		
		// Retrieve the arguments.
		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		
		// Fetch the Crime based on the crimeId
		mCrime = CrimeLab.get(getActivity() ).getCrime(crimeId);
		
		// Enable the options menu callback.
		setHasOptionsMenu(true);
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		Log.d(TAG, "Entered: onCreateView()");
		
		// Get reference to the layout.
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
		// Turn on the Up button.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			// If a parent activity is registered in the manifest file, enable the Up button.
			if (NavUtils.getParentActivityIntent(getActivity() ) != null)
			{
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
			else
			{
				Log.d(TAG, "Couldn't enable the Up button");
			}
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
		
		// --- Photo Button ---
		
		mBtnPhoto = (ImageButton)v.findViewById(R.id.crime_imageButton);
		mBtnPhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				// Start the camera.
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
	
	@Override
	public void onPause()
	{
		super.onPause();
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
	 */
	private void showPhoto()
	{
		Photo photo = mCrime.getPhoto();
		BitmapDrawable bitmap= null;
		
		// Get a scaled bitmap.
		if (photo != null)
		{
			String path = getActivity().getFileStreamPath(photo.getFilename()).getAbsolutePath();
			bitmap = PictureUtils.getScaledDrawable(getActivity(), path);
		}
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
		
		if (requestCode == REQUEST_DATE)
		{
			// Retrieve data from the passed-in Intent.
			Date date = (Date) resultData.getSerializableExtra(EXTRA_DATE);
			
			// Update the date in the model layer(CrimeLab)
			mCrime.setDate(date);
			
			// Set the updated date on the mBtnDate.
			showUpdatedDate();
		}
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
				
				// Log.i(TAG, "filename: " + filename);
				//Log.i(TAG, "Crime: " +  mCrime.getTitle() + " has a photo");
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
				
				// Get the crime title.
				String crimeTitle = (mCrime.getTitle() == null || mCrime.getTitle().equals("")) ?
						"(No title)" : mCrime.getTitle();
				
				// Delete the crime.
				CrimeLab.get(getActivity()).deleteCrime(mCrime);
				
				// Update the pager adapter.
				((PagerActivity)getActivity()).getPagerAdapter().notifyDataSetChanged();

				// Toast a message and finish this activity.
				Toast.makeText(getActivity(), crimeTitle +" has been deleted.", Toast.LENGTH_SHORT).show();
				getActivity().finish();
				
			default:
				return super.onOptionsItemSelected(item);
	 	}
	}

}  // end class
