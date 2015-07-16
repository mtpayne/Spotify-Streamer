/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mpayne.android.spotifystreamer;

import android.content.Context;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Adapter for populating track list view.
 */
public class TrackAdapter extends ArrayAdapter<Track> {

    ArrayList<Track> tracks;

    public TrackAdapter(Context context, int resourceID, ArrayList<Track> tracks) {
        super(context, 0, tracks);
        this.tracks = tracks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_track, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = tracks.get(position);

        viewHolder.trackName.setText(track.name);
        viewHolder.albumName.setText(track.albumName);
        if(null != track.imageUrlSmall && Patterns.WEB_URL.matcher(track.imageUrlSmall).matches())
        {
            Picasso.with(getContext()).load(track.imageUrlSmall).into(viewHolder.albumImage);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView trackName;
        TextView albumName;
        ImageView albumImage;

        public ViewHolder(View view) {
            trackName = (TextView) view.findViewById(R.id.textview_track_name);
            albumName = (TextView) view.findViewById(R.id.textview_album_name);
            albumImage = (ImageView) view.findViewById(R.id.imageview_track_album);
        }
    }
}
