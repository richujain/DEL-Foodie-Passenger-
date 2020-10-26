package com.example.delfoodiepassenger;
import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MyListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] maintitle;
    private final String[] subtitle;
    private final String[] imgid;
    private final String[] coverid;

    public MyListAdapter(Activity context, String[] maintitle, String[] subtitle, String[] imgid, String[] coverid) {
        super(context, R.layout.restaurants_listview_layout, maintitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.maintitle=maintitle;
        this.subtitle=subtitle;
        this.imgid=imgid;
        this.coverid=coverid;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.restaurants_listview_layout, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);
        ImageView coverView = (ImageView) rowView.findViewById(R.id.coverPhoto);

        titleText.setText(maintitle[position]);
        //imageView.setImageResource(imgid);
        Glide.with(context).load(imgid[position]).centerCrop().into(imageView);
        Glide.with(context).load(coverid[position]).centerInside().into(coverView);

        subtitleText.setText(subtitle[position]);

        return rowView;

    };
}