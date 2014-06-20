package com.example.ranter.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
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
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements Replication.ChangeListener{

    public static final String SYNC_URL = "http://procouchbase.cloudapp.net:4984/ranter";  // 10.0.2.2 == Android Simulator equivalent of 127.0.0.1

    byte[] image;
    Database ranterDb;

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
            ranterDb = manager.getDatabase("ranter");
            ViewsInit init = new ViewsInit(ranterDb);
            init.init(((RanteRApplication) getApplication()).getUserName());

        } catch (Exception e) {
            Log.e(Log.TAG, "Error: " + e);
        }

        // Create Couchbase Sync Gateway replication:
        if(ranterDb != null) {
            URL url;
            try {
                url = new URL(SYNC_URL);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            Replication pull = ranterDb.createPullReplication(url);
            pull.setContinuous(true);

            Replication push = ranterDb.createPushReplication(url);
            push.setContinuous(true);

            pull.start();
            push.start();

            pull.addChangeListener(this);
            push.addChangeListener(this);
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
                doc.put("userName", ((RanteRApplication) getApplication()).getUserName());
                doc.put("date", now);
                doc.put("type", "rant");
                doc.put("rantText", ((EditText) findViewById(R.id.rantText)).getText().toString().trim());


                Manager manager = ((RanteRApplication) getApplication()).getCouchbaseManager();

                try {

                    Database ranterDb = manager.getDatabase("ranter");

                    Document document = ranterDb.createDocument();
                    document.putProperties(doc);

                    if(image != null) {

                        // Create a media file name
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                        String fileName  = "IMG_"+ timeStamp + ".jpg";
                        InputStream stream =new ByteArrayInputStream(image);

                        UnsavedRevision newRev = document.getCurrentRevision().createRevision();
                        newRev.setAttachment(fileName, "image/jpeg", stream);
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

                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, 1);

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

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                image = data.getByteArrayExtra("result");
                previewImage();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
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

            final Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(image),
                                                             new Rect(0,0,0,0),
                                                             options);

            imgPreview.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changed(Replication.ChangeEvent event) {

        Replication replication = event.getSource();
        android.util.Log.d("Sync", "Replication : " + replication + " changed.");
        if (!replication.isRunning()) {
            String msg = String.format("Replication %s is stopped", replication);
            android.util.Log.d("Sync", msg);
        }
        else {
            int processed = replication.getCompletedChangesCount();
            int total = replication.getChangesCount();
            String msg = String.format("Changes processed %d / %d", processed, total);
            android.util.Log.d("Sync", msg);
        }
    }
}