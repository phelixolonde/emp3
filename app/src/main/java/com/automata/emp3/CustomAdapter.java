package com.automata.emp3;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Song> implements View.OnClickListener{

    private ArrayList<Song> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtTitle;
        TextView txtArtist;
        ImageView image;
    }

    public CustomAdapter(ArrayList<Song> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {


    }
    @Override
    public Song getItem(int position) {
        return dataSet.get(position);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Song song = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtArtist = convertView.findViewById(R.id.artist);
            viewHolder.txtTitle = convertView.findViewById(R.id.title);
            viewHolder.image = convertView.findViewById(R.id.image);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }



        viewHolder.txtTitle.setText(song.getTitle());
        viewHolder.txtArtist.setText(song.getArtist());

        try{
            Picasso.get().load(song.getImage()).placeholder(R.drawable.music).into(viewHolder.image);
        }catch (Exception e){
            //Log.e("ALBUM_ART",e.getMessage(),e);
        }

        return convertView;
    }
}
