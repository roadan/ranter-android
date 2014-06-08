package com.example.ranter.app;

import android.app.Application;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import org.apache.commons.lang3.time.StopWatch;


/**
 * Created by roadan on 6/4/14.
 */
public class RanteRApplication extends Application {

    private Manager manager;

    @Override
    public void onCreate() {

        StopWatch watch = new StopWatch();
        final String TAG = "RantRApplication";

        try {

            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            manager.enableLogging(TAG, Log.INFO);

            Log.i(TAG, "manager created");

            // create a name for the database and make sure the name is legal
            String dbname = "ranter";
            if (!Manager.isValidDatabaseName(dbname)) {

                Log.e(TAG, "Bad database name");
                return;

            }

            // create a new database
            Database database = manager.getDatabase(dbname);

            Log.d (TAG, "Database created/retrieved");

        }
        catch (Exception e) {

            Log.e(TAG, "Error: " + e);

        }
    }

    public Manager getCouchbaseManager() {

        return manager;

    }


}