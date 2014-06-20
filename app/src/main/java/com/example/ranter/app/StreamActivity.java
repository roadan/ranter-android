package com.example.ranter.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.util.Log;

import java.util.Iterator;


public class StreamActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        Manager manager = ((RanteRApplication) getApplication()).getCouchbaseManager();
        try {

            // create a new database
            Database ranterDb = manager.getDatabase("ranter");

            // Set up a query for a view that indexes blog posts, to get the latest:
            Query query = ranterDb.getView("Stream").createQuery();
            query.setDescending(true);
            query.setLimit(20);

            QueryEnumerator rants = query.run();

            String[] ranters = new String[rants.getCount()];
            String[] rantTexts = new String[rants.getCount()];

            int i = 0;

            for (Iterator<QueryRow> it = rants; it.hasNext(); ) {
                QueryRow row = it.next();
                Document doc = ranterDb.getDocument(row.getDocumentId());

                ranters[i] = doc.getProperty("userName").toString();
                rantTexts[i] = doc.getProperty("rantText").toString();
                i++;
            }

            ListView rantsView = (ListView) findViewById(R.id.list);
            RantsListAdapter rantsAdapter = new RantsListAdapter(this, R.layout.row, ranters, rantTexts);

            rantsView.setAdapter(rantsAdapter);

        } catch (Exception e) {
            Log.e(Log.TAG, "Error: " + e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stream, menu);
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
        return super.onOptionsItemSelected(item);
    }

}
