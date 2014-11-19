package com.mnishiguchi.android.criminalintent;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CrimeCameraFragment extends Fragment
{
	public final String TAG = "CriminalIntent : " + getClass().getSimpleName();
	
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		// Layout view.
		View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);
		
		Button btnTakePicture = (Button)v.findViewById(R.id.crime_camera_btnTakePicture);
		btnTakePicture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				getActivity().finish();  // Temp.
			}
		});

		mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
		
		return v;
	}
	
	
}
