package com.example.ranter.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Manager manager = ((RanteRApplication)getApplication()).getCouchbaseManager();
        try {

            // create a new database
            Database ranterDb = manager.getDatabase("ranter");

        }
        catch (Exception e){
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

                Manager manager = ((RanteRApplication)getApplication()).getCouchbaseManager();

                try {
                    Database ranterDb = manager.getDatabase("ranter");

                    Document document = ranterDb.createDocument();
                    document.putProperties(doc);

                    Log.d (Log.TAG, "Document written to database ranter with Id = " + document.getId());

                    // retrieve the document from the database
                    Document retrievedDocument = ranterDb.getDocument(document.getId());

                    // display the retrieved document
                    Log.d(Log.TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
                } catch (CouchbaseLiteException e) {
                    Log.e(Log.TAG, "Cannot write document to database", e);
                }
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
}