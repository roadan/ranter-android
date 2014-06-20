package com.example.ranter.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RantsListAdapter extends ArrayAdapter<String> {

    private Activity context;
    private int resource;
    String[] ranters;
    String[] rants;

    public RantsListAdapter(Activity context, int resource, String[] ranters, String[] rants) {

        super(context, resource, ranters);
        this.context = context;
        this.resource = resource;
        this.ranters = ranters;
        this.rants = rants;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(this.resource, null, true);

        TextView txtUserName = (TextView) rowView.findViewById(R.id.rowUserName);
        TextView txtRant = (TextView) rowView.findViewById(R.id.rowRant);

        txtUserName.setText(ranters[position]);
        txtRant.setText(rants[position]);

        return rowView;

    }
}
