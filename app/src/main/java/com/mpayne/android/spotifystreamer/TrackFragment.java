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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Searches for and displays artist top tracks.
 */
public class TrackFragment extends Fragment {

    private final String LOG_TAG = TrackFragment.class.getSimpleName();
    private final String PARCELABLE_KEY_TRACKS = "tracks";
    private final String NO_TRACKS_FOUND_MESSAGE = "No tracks found. Please try another artist.";

    private TrackAdapter mTrackAdapter;
    private TextView mMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);
        ListView mTrackListView = (ListView) rootView.findViewById(R.id.listview_track);

        mTrackAdapter = new TrackAdapter(getActivity(), R.layout.listitem_track, new ArrayList<Track>());
        mTrackListView.setAdapter(mTrackAdapter);
        mMessage = (TextView) rootView.findViewById(R.id.textview_message);

        // Check Intent for artistId.
        String artistId = "";
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        // Check savedInstanceState for track list and message on orientation change.
        if (savedInstanceState != null) {
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(PARCELABLE_KEY_TRACKS);
            if (parcelables != null) {
                for (Parcelable parcelable : parcelables) {
                    mTrackAdapter.add(((Track) parcelable));
                }
            }
            // If no track data display message
            if(mTrackAdapter.isEmpty()) {
                mMessage.setText(NO_TRACKS_FOUND_MESSAGE);
                mMessage.setVisibility(View.VISIBLE);
            } else {
                // Don't show empty message for spacing reasons.
                mMessage.setVisibility(View.GONE);
            }
        } else {
            // Don't show empty message for spacing reasons.
            mMessage.setVisibility(View.GONE);
            // Search for tracks.
            new SearchTrackTask().execute(artistId);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save track list if available.
        if (mTrackAdapter.getCount() > 0) {
            Parcelable[] parcelables = new Parcelable[mTrackAdapter.getCount()];
            for (int i = 0; i < mTrackAdapter.getCount(); i++) {
                parcelables[i] = mTrackAdapter.getItem(i);
            }
            outState.putParcelableArray(PARCELABLE_KEY_TRACKS, parcelables);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     *  Background task for retrieving and populating track list.
     */
    public class SearchTrackTask extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = SearchTrackTask.class.getSimpleName();

        @Override
        protected Tracks doInBackground(String... params) {

            // Exit empty track searches.
            if (params.length == 0) {
                return null;
            }

            // Use SpotifyApi to search for tracks using artistId and Country code.
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();

            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
            return spotifyService.getArtistTopTrack(params[0], options);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            if (tracks != null) {
                mTrackAdapter.clear();
                for (kaaes.spotify.webapi.android.models.Track track : tracks.tracks) {
                    mTrackAdapter.add(new Track(track));
                }
                // Display message if no tracks returned from search.
                if(mTrackAdapter.isEmpty()) {
                    mMessage.setText(NO_TRACKS_FOUND_MESSAGE);
                    mMessage.setVisibility(View.VISIBLE);
                }
            }
        }

    }
}
