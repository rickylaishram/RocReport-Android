package com.rocreport.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rocreport.android.data.MainData;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Vector;

/**
 * Created by rickylaishram on 11/15/13.
 */
public class MainAdapter extends ArrayAdapter <MainData>{
    Context context;
    int layoutResourceId;
    Vector<MainData> data = new Vector<MainData>();

    public MainAdapter(Context context, int layoutResourceId, Vector<MainData> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.iv_back = (ImageView) row.findViewById(R.id.image);
            holder.iv_voted = (ImageView) row.findViewById(R.id.green);
            holder.tv_title = (TextView) row.findViewById(R.id.title);
            holder.tv_category = (TextView) row.findViewById(R.id.category);
            holder.tv_date = (TextView) row.findViewById(R.id.date);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        Typeface font_black = Typeface.createFromAsset(context.getAssets(), "Roboto-Black.ttf");

        MainData item = data.elementAt(position);
        holder.tv_title.setText(item.loc_name);
        holder.tv_category.setText(item.category);
        holder.tv_date.setText(item.created);

        holder.tv_title.setTypeface(font_black);
        holder.tv_category.setTypeface(font_black);
        holder.tv_date.setTypeface(font_black);

        Picasso.with(context).load(item.picture).into(holder.iv_back);

        if(!item.has_voted) {
            holder.iv_voted.setVisibility(View.INVISIBLE);
        }

        return row;
    }

    static class Holder {
        public TextView tv_title;
        public TextView tv_category;
        public ImageView iv_back;
        public ImageView iv_voted;
        public TextView tv_date;
    }
}
