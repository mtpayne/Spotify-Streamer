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
 * Adapter for populating artist list view.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {

    ArrayList<Artist> artists;

    public ArtistAdapter(Context context, int resourceID, ArrayList<Artist> artists) {
        super(context, 0, artists);
        this.artists = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_artist, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Artist artist = artists.get(position);
        viewHolder.artistName.setText(artist.name);
        if(artist.imageUrlSmall != null && Patterns.WEB_URL.matcher(artist.imageUrlSmall).matches())
        {
            Picasso.with(getContext()).load(artist.imageUrlSmall).into(viewHolder.artistImage);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView artistName;
        ImageView artistImage;

        public ViewHolder(View view) {
            artistName = (TextView) view.findViewById(R.id.textview_artist_name);
            artistImage = (ImageView) view.findViewById(R.id.imageview_artist);
        }
    }
}
