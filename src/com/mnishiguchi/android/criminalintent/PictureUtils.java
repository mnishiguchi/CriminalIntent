package com.mnishiguchi.android.criminalintent;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

/**
 * Utility class that provides static methods for image handling.
 * Pictures will easily blow out your app's memory budget.
 * So you need some code to scale the image before loading it and
 * some code to clean up when the image is no longer needed.
 */
public class PictureUtils
{
	private static final String TAG = "CriminalIntent.PictureUtils";
	
	/**
	 * Get a BitmapDrawable from a local file that is scaled down to fit the current Window size.
	 */
	public static BitmapDrawable getScaledDrawable(Activity activity, String path)
	{
		// Get  the dimensions of the display.
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		float destWidth = size.x;
		float destHeight = size.y;
		
		// Get the dimensions of the image on disk.
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // No pixel data needed.
		BitmapFactory.decodeFile(path, options); 
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		// Initialize inSampleSize.
		int inSampleSize = 1; // Read every ??? px.
		
		// Check if the image is larger than the display.
		if (srcHeight > destHeight || srcWidth > destWidth)
		{
			if (srcWidth > srcHeight) // Landscape
			{
				// Let the height match.
				inSampleSize = Math.round(srcHeight / destHeight);
			}
			else // Portrait
			{
				// Let the width match.
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}
		
		// Set the inSampleSize.
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		// Scale down the bitmap data based on the inSampleSize.
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		
		// Create drawable from the scaled bitmap.
		return new BitmapDrawable(activity.getResources(), bitmap);
	}
	
	/**
	 * Get an image data as byte array and create a BitmapDrawable that is 
	 * scaled it down to fit the current Window size.
	 */
	public static Bitmap getScaledBitmap(Activity activity, byte[] data)
	{
		// Get  the dimensions of the display.
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		float destWidth = size.x;
		float destHeight = size.y;
		
		// Get the dimensions of the image on disk.
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // No pixel data needed.
		Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length, options);
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		// Initialize inSampleSize.
		int inSampleSize = 1; // Read every ??? px.
		
		// Check if the image is larger than the display.
		if (srcHeight > destHeight || srcWidth > destWidth)
		{
			if (srcWidth > srcHeight) // Landscape
			{
				// Let the height match.
				inSampleSize = Math.round(srcHeight / destHeight);
			}
			else // Portrait
			{
				// Let the width match.
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}
		
		// Set the inSampleSize.
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		// Scale down the bitmap data based on the inSampleSize.
		bitmap = BitmapFactory.decodeByteArray(data , 0, data.length, options);
		
		// Create drawable from the scaled bitmap.
		return bitmap;
	}
	
	/**
	 * Compress the bitmap.
	 */
	public static Bitmap compressBitmap(Bitmap bitmap)
	{
		// Write a compressed version of the bitmap to the specified outputstream.
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 80, out);
		Bitmap compressedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
		
		Log.e("Original   dimensions", bitmap.getWidth() + " " + bitmap.getHeight());
		Log.e("Compressed dimensions", compressedImage.getWidth() + " " + compressedImage.getHeight());
		
		return compressedImage;
	}

	/**
	 * Get a bitmap drawable from disk for the specified photo object.
	 */
	public static BitmapDrawable loadBitmapDrawableFromFile(Activity activity, Photo photo)
	{
		BitmapDrawable bitmap = null;
		// Get a scaled bitmap.
		if (photo != null)
		{
			// Get the absolute path of the photo file on the filesystem. 
			String path = activity.getFileStreamPath(photo.getFilename())
					.getAbsolutePath(); // Convert the path to string.
			
			// Get a scaled bitmap drawable based on the data in this file.
			bitmap = PictureUtils.getScaledDrawable(activity, path);
		}
		
		return bitmap;
	}
	
	/**
	 * Explicitly clean up an ImageView's BitmapDrawable, if it has one.
	 * This can prevent the possibility of ugly memory bugs.
	 * Loading images in onStart() and them unloading in onStop is a good practice.
	 */
	public static void cleanImageView(ImageView imageView)
	{
		// Ensure that the passed-in view is of type BitmapDrawable.
		if (!(imageView.getDrawable() instanceof BitmapDrawable))
		{
			return;
		}
			
		// Clear the reference to the image's pixel data.
		BitmapDrawable bitmap = (BitmapDrawable)imageView.getDrawable();
		bitmap.getBitmap().recycle(); 
		
		// Clear the imageView.
		imageView.setImageDrawable(null);
	}
	
	/**
	 * 
	 */
	public static boolean savePictureInternal(Context context, byte[] data, String filename)
	{
		// Save the jpeg data to disk.
		FileOutputStream out = null;
		try
		{
			out = context.openFileOutput(filename, Context.MODE_PRIVATE);
			out.write(data);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error closing file: " + filename, e);	
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	public static boolean savePictureExternal(Context context, byte[] data, String filename)
	{
		// Ensure that the external storage is available.
		if (! Environment. getExternalStorageState (). equals (Environment .MEDIA_MOUNTED ))
		{
			Log.e(TAG, "External Storage is not available." );
			return false;
		}

		// Save the jpeg data to disk.
		BufferedOutputStream out = null;
		try
		{
			File path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(path, filename);
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(data);
			out.close();
			
			// Get the media scanner service tol read metadata from the file and
			// add the file to the media content provider. 
			/*MediaScannerConnection.scanFile(getActivity(),
					new String[] { file.toString() }, null,
					new MediaScannerConnection.OnScanCompletedListener()
					{
						@Override
						public void onScanCompleted(String path, Uri uri)
						{
							Log.i("ExternalStorage", "Scanned " + path + ":");
							Log.i("ExternalStorage", "-> uri=" + uri);
						}
					});*/
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error saving photo file: " + filename, e);
			return false;
		}
		return true;
	}
	
	public static void saveImage(Context context, Bitmap finalBitmap) {

	    String root = Environment.getExternalStorageDirectory().toString();
	    File myDir = new File(root + "/saved_images");    
	    myDir.mkdirs();
	    Random generator = new Random();
	    int n = 10000;
	    n = generator.nextInt(n);
	    String fname = "Image-"+ n +".jpg";
	    File file = new File (myDir, fname);
	    if (file.exists ()) file.delete (); 
	    try {
	           FileOutputStream out = new FileOutputStream(file);
	           finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	           out.flush();
	           out.close();

	    } catch (Exception e) {
	           e.printStackTrace();
	    }
	}
	
}
