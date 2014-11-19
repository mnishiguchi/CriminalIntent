package com.mnishiguchi.android.criminalintent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CrimeCameraFragment extends Fragment
{
	private static final String TAG = "tag_CrimeCameraFragment";
	
	public static final String EXTRA_PHOTO_FILENAME =
			"com.mnishiguchi.android.criminalintent.photo_filename";
	
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private View mProgressContainer;
	
	/*
	 * Camera Callbacks for Camera.takePicture(...)
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
	 * Camera Callbacks for Camera.takePicture(...)
	 */
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// Create a file name.
			String filename = UUID.randomUUID().toString() + ".jpeg";
			
			// Save the jpeg data to disk.
			FileOutputStream out = null;
			boolean success = true;
			try
			{
				out = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
				out.write(data);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Error closing file: " + filename, e);
				success = false;
			}
			
			if (success)
			{
				Intent i = new Intent();
				i.putExtra(EXTRA_PHOTO_FILENAME, filename);
				getActivity().setResult(Activity.RESULT_OK, i);
			}
			else
			{
				getActivity().setResult(Activity.RESULT_CANCELED);
			}
			getActivity().finish();
		}
	};
	
	@SuppressWarnings("deprecation")
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
		
		// Deprecated but required for Camera Preview to work on pre-3.0 devices.
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
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
		
		mCamera = Camera.open(0);
	
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if (mCamera != null) // Camera exists?
		{
			mCamera.release();
			mCamera = null;
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
}