package com.example.oscar.androidcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.camera2.*;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    static final int REQUEST_TAKE_PHOTO = 1;

    ImageView mImageView = null;
    String mCurrentPhotoPath = null;    // Ruta completa de la foto cuando se haga

    String photoName = "Javier Gimenez";    // Internamente se guardará como Javier Gimenez-numeroAleatorio.jpg ==> Javier Gimenez-45378563484.jpg

    /*
    En la memoria del teléfono se guardará la siguiente información:

    [NAME]
    Javier Gimenez

    [ROUTE]
    /storage/sdcard1/Android/data/com.example.xxxx.yyyyyy/files/Pictures

    [EXTENSION]
    -45378563484.jpg


    Así que la ruta completa será:
    /storage/sdcard1/Android/data/com.example.xxxx.yyyyyy/files/Pictures/Javier Gimenez-45378563484.jpg

     */



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.FotoImage);
    }

    public void makePhoto(View view)
    {
        Log.d("CAMERA", "click done");
        dispatchTakePictureIntent("Javier Gimenez");
    }

    public void loadFoto(View view)
    {
        // Put bitmap from imageFile into imageView
        SharedPreferences prefs = getPreferences(0);;

        String routePictures = prefs.getString("ROUTE", "");
        String name = prefs.getString("NAME", "");
        String extension = prefs.getString("EXTENSION", "");

        String fullPath = prefs.getString("ROUTE", "") + "/" + name + extension;

        if(name.equals("Javier Gimenez"))
            setPic(fullPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Toast.makeText(this, "Foto de " + photoName + " creada!", Toast.LENGTH_SHORT).show();



            // Save route and foto name in shared preferences
            SharedPreferences prefs = getPreferences(0);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("ROUTE", storageDir.getAbsolutePath());
            editor.putString("NAME", photoName);
            int idx = mCurrentPhotoPath.split(photoName).length;
            String extension = mCurrentPhotoPath.split(photoName)[idx - 1];
            editor.putString("EXTENSION", extension);
            editor.commit();
        }
    }

    private File createImageFile(String imageFileName) throws IOException
    {
        // Create an image file
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(String imageFile)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile(imageFile);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR", "Error al crear el fichero de la foto.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void setPic(String imagePath)
    {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }
}