package com.mnishiguchi.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
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
	
}
