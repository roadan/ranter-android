package com.example.ranter.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
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

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private Camera camera;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private Uri fileUri;

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

                    Log.d(Log.TAG, "Document written to database ranter with Id = " + document.getId());

                    // retrieve the document from the database
                    Document retrievedDocument = ranterDb.getDocument(document.getId());

                    // d    isplay the retrieved document
                    Log.d(Log.TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));

                } catch (CouchbaseLiteException e) {
                    Log.e(Log.TAG, "Cannot write document to database", e);
                }
            }

        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getPictureFileUri();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                // start the image capture Intent
                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }

            private Uri getPictureFileUri() {
                File sdDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File renterDir = new File(sdDir, "RanteRCamera");

                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new java.util.Date());

                File mediaFile = new File(renterDir.getPath() + File.separator
                        + "IMG_" + timeStamp + ".jpg");

                return Uri.fromFile(mediaFile);

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

                // image captured, need to display and
                // attach it to the document
                previewImage();

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

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}