package com.hibonit.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class AddPhotos extends Activity {
	private static int TAKE_PHOTO = 1;
	private static int SELECT_PHOTO = 2;
	private Button buttonTakePhoto;
	private Button buttonGalleryPhoto;
	private Bitmap bitmap;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_photos);
		buttonTakePhoto = (Button) findViewById(R.id.button_take_photo);
		buttonTakePhoto.setOnClickListener(photoListener);
		buttonGalleryPhoto = (Button) findViewById(R.id.button_photo_from_gallery);
		buttonGalleryPhoto.setOnClickListener(galleryPhotoListener);
	}
	
	OnClickListener photoListener = new OnClickListener() {			
		public void onClick(View v) {
			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    startActivityForResult(takePictureIntent, TAKE_PHOTO);
		}
	};
	
	OnClickListener galleryPhotoListener = new OnClickListener() {			
		public void onClick(View v) {
			Intent intent = new Intent();
		      intent.setType("image/*");
		      intent.setAction(Intent.ACTION_GET_CONTENT);
		      startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PHOTO);
		}
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  Uri photoUri = data.getData();
//        if (requestCode == TAKE_PHOTO && resultCode == Activity.RESULT_OK){
//        	
//            try {
//            	
//                if (bitmap != null) {
//                    bitmap.recycle();
//                }
//                InputStream stream = getContentResolver().openInputStream(
//                        data.getData());
//                bitmap = BitmapFactory.decodeStream(stream);
//                Log.v("hibonit", "bitmap taken");
//                stream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }else if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK){
//        	Uri uri = data.getData();
//        	String[] projection = { MediaStore.Images.Media.DATA };
//            Cursor cursor = managedQuery(uri, projection, null, null, null);
//            int column_index = cursor
//                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            String imagePath = cursor.getString(column_index);             
//        }
        super.onActivityResult(requestCode, resultCode, data);
    } 
}
