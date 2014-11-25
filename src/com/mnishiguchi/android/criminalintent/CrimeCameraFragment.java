package com.mnishiguchi.android.criminalintent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class CrimeCameraFragment extends Fragment
{
	private static final String TAG = "CriminalIntent.CrimeCameraFragment";
	
	public static final String EXTRA_PHOTO_FILENAME = "com.mnishiguchi.android.criminalintent.photo_filename";
	public static final String EXTRA_PHOTO_ORIENTATION = "com.mnishiguchi.android.android.criminalintent.photo_orientation";
	
	private OrientationEventListener mOrientationEventListener;
	private Orientation mOrientation;
	
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private View mProgressContainer;
	
	/*
	 * A Camera Callback for Camera.takePicture(...).
	 * Show the progress indicator and intercept any touch events.
	 */
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

		@Override
		public void onShutter()
		{
			// Display the progress indicator.
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};
	
	/*
	 * A Camera Callback for Camera.takePicture(...)
	 * Save the picture(jpeg) to disk.
	 * Send the result to CrimeFragment.
	 */
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// Create a file name with a random UUID.
			String filename = UUID.randomUUID().toString() + ".jpeg";
			
			// Scale down the bitmap to a smaller size.
			Bitmap bitmap = PictureUtils.getScaledBitmap(getActivity(), data);
			
			// Compress the bitmap.
			bitmap = PictureUtils.compressBitmap(bitmap);
			
			// Save the picture on disk.
			boolean success = PictureUtils.savePictureExternalPrivate(getActivity(), data, filename);
			
			if (success) // Successfully saved.
			{
				Intent i = new Intent();
				i.putExtra(EXTRA_PHOTO_FILENAME, filename);
				int orientation = mOrientation.mode;
				i.putExtra(EXTRA_PHOTO_ORIENTATION, orientation);
				getActivity().setResult(Activity.RESULT_OK, i);
			}
			else // Error occurred.
			{
				getActivity().setResult(Activity.RESULT_CANCELED);
			}
			getActivity().finish();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		// --- Layout View ---
		View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);
		
		// --- Progress Container ---
		mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
		mProgressContainer.setVisibility(View.INVISIBLE);
		
		// --- Take Button ---
		Button btnTakePicture = (Button)v.findViewById(R.id.crime_camera_btnTakePicture);
		btnTakePicture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				//getActivity().finish();  // Temp.
				if (mCamera != null)
				{
					mCamera.takePicture(mShutterCallback, null, mJpegCallback);
				}
			}
		});

		// --- Camera Preview ---
		
		mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
		
		// The Camera sends image data to the Surface via SurfaceHolder.
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceCreated(SurfaceHolder holder)
			{
				// Tell the camera to use this surface as its preview area.
				try
				{
					if (mCamera != null)
					{
						mCamera.setPreviewDisplay(holder);
					}
				}
				catch (IOException e)
				{
					Log.e(TAG, "Error setting up preview display", e);
				}
			}
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder)
			{
				// Stop the preview.
				if (mCamera != null)
				{
					mCamera.stopPreview();
				}
			}
			
			// The surface has changed size.
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
			{
				Camera.Parameters parameters = mCamera.getParameters();
				
				// Update the camera preview size.
				Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
				parameters.setPreviewSize(s.width, s.height);
				
				// Set the picture size.
				// The camera needs to know what size picture to create.
				s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
				parameters.setPictureSize(s.width, s.height);
				
				mCamera.setParameters(parameters);
				
				try // If parameter is invalid, exceptions will be thrown.
				{
					mCamera.startPreview();
				}
				catch (Exception e)
				{
					Log.e(TAG, "Couldn't start preview", e);
					mCamera.release();
					mCamera = null;
				}
			}
		});
		
		return v;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Open the camera resource.
		mCamera = Camera.open(0);
		
		// Global storage for orientation data.
		mOrientation = Orientation.get(getActivity());
		
		// Orientation Detector
		if (null == mOrientationEventListener)
		{
			mOrientationEventListener = new OrientationEventListener(getActivity(),
					SensorManager.SENSOR_DELAY_NORMAL) {

					@Override
					public void onOrientationChanged(int orientation)
					{
						// determine our orientation based on sensor response
						if (orientation >= 315 || orientation < 45)
						{
							if (mOrientation.mode != Orientation.PORTRAIT_NORMAL)
							{
								mOrientation.mode = Orientation.PORTRAIT_NORMAL;
							}
						}
						else if (orientation < 315 && orientation >= 225)
						{
							if (mOrientation.mode != Orientation.LANDSCAPE_NORMAL)
							{
								mOrientation.mode = Orientation.LANDSCAPE_NORMAL;
							}
						}
						else if (orientation < 225 && orientation >= 135)
						{
							if (mOrientation.mode != Orientation.PORTRAIT_INVERTED)
							{
								mOrientation.mode = Orientation.PORTRAIT_INVERTED;
							}
						}
						else // orientation <135 && orientation > 45
						{ 
							if (mOrientation.mode != Orientation.LANDSCAPE_INVERTED)
							{
								mOrientation.mode = Orientation.LANDSCAPE_INVERTED;
							}
						}
					}
				};
			}
		
			if (mOrientationEventListener.canDetectOrientation())
			{
				mOrientationEventListener.enable();
			}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if (mCamera != null)
		{
			mCamera.release();
			mCamera = null;
		}
		
		if (mOrientationEventListener != null)
		{
			mOrientationEventListener.disable();
			mOrientation.mode = Orientation.NO_DATA;
		}
	}
	
	/**
	 * A simple algorithm to get the largest size available based on the list of supported sizes.
	 */
	private Size getBestSupportedSize(List<Size> sizes, int width, int height)
	{
		// Initialize best size to the first one of the list.
		Size bestSize = sizes.get(0);
		int largestArea = bestSize.width * bestSize.height;
		
		// Check all the supported sizes and find the one with the largest area.
		for (Size size : sizes)
		{
			int area = size.width * size.height;
			if (area > largestArea)
			{
				bestSize = size;
				largestArea = area;
			}
		}
		
		return bestSize;
	}
	
	/**
	 * Show a toast message.
	 */
	private void showToast(String msg)
	{
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
}
