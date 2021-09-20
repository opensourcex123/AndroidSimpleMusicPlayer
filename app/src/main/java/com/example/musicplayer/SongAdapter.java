package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Song> songList;

    public SongAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.songList = songList;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int i) {
        return songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.song_item, null);
            viewHolder = new ViewHolder();
            viewHolder.songName = view.findViewById(R.id.songName);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.songName.setText(songList.get(i).getName());
        return view;
    }

    private static class ViewHolder {
        TextView songName;
    }
}
