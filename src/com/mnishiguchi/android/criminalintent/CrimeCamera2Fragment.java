package com.mnishiguchi.android.criminalintent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.TotalCaptureResult;

@TargetApi(21)
public class CrimeCamera2Fragment  extends Fragment
{
	private static final String TAG = "CriminalIntent.CrimeCamera2Fragment";
	
	public static final String EXTRA_PHOTO_FILENAME = "com.mnishiguchi.android.criminalintent.photo_filename";
	public static final String EXTRA_PHOTO_ORIENTATION = "com.mnishiguchi.android.android.criminalintent.photo_orientation";
	
	private OrientationEventListener mOrientationEventListener;
	private Orientation mOrientation;
	
	private CameraManager mCameraManager;
	private CameraDevice mCameraDevice;
	private CameraCaptureSession mCameraCaptureSession;
	private ImageReader mImageReader;
	private Surface mPreviewSurface;
	private Surface mJpegCaptureSurface;
	
	private TextureView mTextureView;
	private SurfaceView mSurfaceView;
	private View mProgressContainer;
	
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
				if (mCameraDevice != null)
				{
					// TODO take picture
					try
					{
						mCameraCaptureSession.capture(requestJpegCapture(), new CaptureCallback() {
							
							@Override
							public void onCaptureCompleted(CameraCaptureSession session,
									CaptureRequest request, TotalCaptureResult result)
							{
								byte[] data = null;
								
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
									i.putExtra(EXTRA_PHOTO_ORIENTATION, mOrientation.mode);
									getActivity().setResult(Activity.RESULT_OK, i);
								}
								else // Error occurred.
								{
									getActivity().setResult(Activity.RESULT_CANCELED);
								}
								getActivity().finish();
							}
						}, null);
					}
					catch (CameraAccessException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		// --- Camera Preview ---
		mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);

		return v;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Open the camera resource.
		//mCamera = Camera.open(0);
		
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
		
		//if (mCamera != null)
		{
		//	mCamera.release();
		//	mCamera = null;
		}
		
		if (mOrientationEventListener != null)
		{
			mOrientationEventListener.disable();
			mOrientation.mode = Orientation.NO_DATA;
		}
	}

	public void openCamera()
	{
		mCameraManager = (CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
		
		try
		{
			mCameraManager.openCamera("0", new CameraDevice.StateCallback() {

				@Override
				public void onOpened(CameraDevice camera)
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onDisconnected(CameraDevice camera)
				{
					// TODO Auto-generated method stub
				}

				@Override
				public void onError(CameraDevice camera, int error)
				{
					// TODO Auto-generated method stub
				}
			}, null);
			
			// Configure and create surfaces.
			List<Surface> surfaces = createSurfaces();
			
			// Start the capture session.
			mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

				@Override
				public void onConfigured(CameraCaptureSession session)
				{
					Log.e(TAG, "createCaptureSession - onConfigured()");
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession session)
				{
					Log.e(TAG, "createCaptureSession - onConfigureFailed()");
				}
			}, 	null);
		}
		catch (CameraAccessException e)
		{
			Log.e(TAG, "Error opening the camera", e);
		}
	}
	
	private List<Surface> createSurfaces()
	{
		// Get the configuration map
		CameraCharacteristics characteristics = null;
		StreamConfigurationMap configs = null;
		try
		{
			characteristics = mCameraManager.getCameraCharacteristics("0");
			configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
		}
		catch (CameraAccessException e)
		{
			Log.e(TAG, "Error trying to get the configuration map: ", e);
		}
		
		// Calculate the best size.
		Size[] sizes = configs.getOutputSizes(ImageFormat.JPEG);
		Size bestSize = getBestSupportedSizeForDisplay(sizes);
		
		// Create surfaces.
		int width = bestSize.getWidth();
		int height = bestSize.getHeight();
		int format = ImageFormat.JPEG;
		int maxImages = 2;
		mImageReader = ImageReader.newInstance(width, height, format, maxImages);
		
		mPreviewSurface = mImageReader.getSurface();
		mJpegCaptureSurface = mImageReader.getSurface();

		List<Surface> surfaces = new ArrayList<Surface>();
		surfaces.add(mPreviewSurface);
		surfaces.add(mJpegCaptureSurface);
		
		return surfaces;
	}
	
	private CaptureRequest requestPreview()
	{
		CaptureRequest.Builder previewRequestBuilder = null;
		try
		{
			previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
		}
		catch (CameraAccessException e)
		{
			Log.e(TAG, "Error requesting preview: ", e);
		}
		
		previewRequestBuilder.addTarget(mPreviewSurface);
		CaptureRequest previewRequest = previewRequestBuilder.build();
		return previewRequest;
	}
	
	private CaptureRequest requestJpegCapture()
	{
		CaptureRequest.Builder captureRequestBuilder = null;
		try
		{
			captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
		}
		catch (CameraAccessException e)
		{
			Log.e(TAG, "Error requesting JpegCapture: ", e);
		}
		captureRequestBuilder.addTarget(mJpegCaptureSurface);
		CaptureRequest captureRequest = captureRequestBuilder.build();
		return captureRequest;
	}
	
	/**
	 * A simple algorithm to get the largest size available based on the list of supported sizes.
	 */
	private Size getBestSupportedSizeForDisplay(Size[] sizes)
	{
		// Get  the dimensions of the display. (Target)
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		int targetArea = p.x * p.y;
		int maxSize = (int) Math.round(targetArea * 2);
		
		// Initialize best size to the first one of the list.
		Size bestSize = sizes[0];
		
		// Check all the supported sizes and find the one larger than display
		// but not too large.
		for (Size size : sizes)
		{
			int area = size.getWidth() * size.getHeight();
			Log.i(TAG, "area: "+ area);
			
			if (area >= bestSize.getWidth() * bestSize.getHeight())
			{
				if (area <= maxSize)
				{
					bestSize = size;
				}
			}
		}
		Log.i(TAG, "targetArea: "+ targetArea);
		Log.i(TAG, "Best Area: "+ bestSize.getWidth() * bestSize.getHeight());
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
