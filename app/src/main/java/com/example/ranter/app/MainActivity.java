package com.example.ranter.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private Uri imageUri;
    InputStream stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = this;
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            Toast.makeText(this, "This device does not have a camera.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Manager manager = ((RanteRApplication) getApplication()).getCouchbaseManager();
        try {

            // create a new database
            Database ranterDb = manager.getDatabase("ranter");

        } catch (Exception e) {
            Log.e(Log.TAG, "Error: " + e);
        }

        Button button = (Button) findViewById(R.id.rantButton);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                // get the current date and time
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar calendar = GregorianCalendar.getInstance();
                String now = dateFormatter.format(calendar.getTime());

                // creating the document as a map
                Map<String, Object> doc = new HashMap<String, Object>();

                doc.put("id", UUID.randomUUID());
                doc.put("date", now);
                doc.put("type", "rant");
                doc.put("rantText", ((EditText) findViewById(R.id.rantText)).getText());

                Manager manager = ((RanteRApplication) getApplication()).getCouchbaseManager();

                try {

                    Database ranterDb = manager.getDatabase("ranter");

                    Document document = ranterDb.createDocument();
                    document.putProperties(doc);

                    if(stream != null) {

                        stream.reset();
                        UnsavedRevision newRev = document.getCurrentRevision().createRevision();
                        newRev.setAttachment(imageUri.getPath(), "image/jpeg", stream);
                        newRev.save();

                    }
                    Log.d(Log.TAG, "Document written to database ranter with Id = " + document.getId());

                    // retrieve the document from the database
                    Document retrievedDocument = ranterDb.getDocument(document.getId());

                    // display the retrieved document
                    Log.d(Log.TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));

                } catch (Exception e) {
                    Log.e(Log.TAG, "Cannot write document to database", e);
                }
            }

        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                // give the image a name so we can store it in the phone's default location
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image (this doesn't work at all for images)
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // store content values
                intent.putExtra( MediaStore.EXTRA_OUTPUT,  imageUri);

                // start the image capture Intent
                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_stream) {
            startActivity(new Intent(this, StreamActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                try {

                    previewImage();
                }

                catch (Exception e){
                    Log.e(Log.TAG, "Error", e);
                }

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "Image capture canceled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewImage() {
        try {

            ImageView imgPreview = (ImageView) findViewById(R.id.imgPreview);
            imgPreview.setVisibility(View.VISIBLE);

            File file = getOutputFile();

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file),
                                                             new Rect(0,0,0,0),
                                                             options);

            imgPreview.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", imageUri);
    }

    /*
     * Here we restore the fileUri again
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageUri = savedInstanceState.getParcelable("file_uri");
    }

    /** Create a File for saving an image or video */
    private static File getOutputFile()
    {
        // To be safe, you should check that the SDCard is mounted

        if(Environment.getExternalStorageState() != null) {

            // this works for Android 2.2 and above
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidCameraTestsFolder");

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()) {
                if (! mediaStorageDir.mkdirs()) {
                    Log.d(Log.TAG, "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;

            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                                 "IMG_"+ timeStamp + ".jpg");

            return mediaFile;
        }

        return null;
    }

}