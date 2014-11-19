package com.mnishiguchi.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity activity, String path)
	{
		Display display = activity.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();
		
		// Read in the dimensions of the image on disk.
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		int inSampleSize = 1;
		if (srcHeight > srcHeight || srcWidth > srcWidth)
		{
			if (srcWidth > srcHeight)
			{
				inSampleSize = Math.round(srcHeight / destHeight);
			}
			else
			{
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}
		
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		
		return new BitmapDrawable(activity.getResources(), bitmap);
	}
	
	/**
	 * Explicitly clean up an ImageView's BitmapDrawable, if it has one.
	 * This can prevent the possibility of ugly memory bugs.
	 */
	public static void cleanImageView(ImageView imageView)
	{
		// Type-checking.
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
