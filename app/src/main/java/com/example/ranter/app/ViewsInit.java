package com.example.ranter.app;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;

import java.util.Map;

public  class ViewsInit {

    Database db;

    public ViewsInit(Database db) {
        this.db = db;
    }
    private static String userName;

    public void init(final String userName) {

        ViewsInit.userName = userName;

        db.deleteViewNamed("Stream");
        View streamView = db.getView("Stream");

        streamView.setMap(new Mapper() {

            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if (document.get("type").equals("rant") &&
                    document.get("userName") != null &&
                    !document.get("userName").equals(ViewsInit.userName) ) {
                    emitter.emit(document.get("userName"), null);

                }
            }
        } , "1");

    }
}
