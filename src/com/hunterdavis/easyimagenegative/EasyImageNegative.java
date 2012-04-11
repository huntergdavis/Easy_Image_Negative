package com.hunterdavis.easyimagenegative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyImageNegative extends Activity {
	
	int SELECT_PICTURE = 22;

	Uri selectedImageUri = null;
	Bitmap photoBitmap = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
        
		// Create an anonymous implementation of OnClickListener
		OnClickListener loadButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Source Photo"),
						SELECT_PICTURE);
			}
		};
		
		
		// Create an anonymous implementation of OnClickListener
		OnClickListener saveButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				Boolean didWeSave = saveImage(v.getContext());
		}
		};
		

		
		Button loadButton = (Button) findViewById(R.id.loadButton);
		loadButton.setOnClickListener(loadButtonListner);
		
		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListner);
		
       
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				selectedImageUri = data.getData();
				ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
				Boolean scaleDisplay = scaleURIAndDisplay(getBaseContext(),
						selectedImageUri, imgView);
			}
		}
	}
    
	public Boolean scaleURIAndDisplay(Context context, Uri uri,
			ImageView imgview) {
		double divisorDouble = 400;
		InputStream photoStream;
		try {
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;

		photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
		if (photoBitmap == null) {
			return false;
		}
		int h = photoBitmap.getHeight();
		int w = photoBitmap.getWidth();
		
		
		
		// This is gonna take up some time....
		Bitmap tempBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		for(int i = 0;i<h;i++)
		{
			for(int j = 0;j<w;j++)
			{
				tempBitmap.setPixel(j, i, (Integer.MAX_VALUE - photoBitmap.getPixel(j, i)));
			}
		}
		
		photoBitmap = tempBitmap;
		
		if ((w > h) && (w > divisorDouble)) {
			double ratio = divisorDouble / w;
			w = (int) divisorDouble;
			h = (int) (ratio * h);
		} else if ((h > w) && (h > divisorDouble)) {
			double ratio = divisorDouble / h;
			h = (int) divisorDouble;
			w = (int) (ratio * w);
		}

		Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
		imgview.setImageBitmap(scaled);
		return true;
	}    
	
	public Boolean saveImage(Context context) {
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		if(selectedImageUri == null)
		{
			return false;
		}
		// actually save the file

		OutputStream outStream = null;
		String newFileName = null;
		

		String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME /* col1 */};
		Cursor c = context.getContentResolver().query(selectedImageUri, projection, null,
				null, null);
		if (c != null && c.moveToFirst()) {
			String oldFileName = c.getString(0);
			int dotpos = oldFileName.lastIndexOf(".");
			if (dotpos > -1) {
				newFileName = oldFileName.substring(0, dotpos) + "-negative.png";
			}
		}
		

		if (newFileName != null) {
			{
				File file = new File(extStorageDirectory, newFileName);
				try {
					outStream = new FileOutputStream(file);
					photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
					try {
						outStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}

					Toast.makeText(context, "Saved " + newFileName,
							Toast.LENGTH_LONG).show();
					new SingleMediaScanner(context, file);

				} catch (FileNotFoundException e) {
					// do something if errors out?
					return false;
				}
			}

			return true;

		}
		return false;
	}
    
}