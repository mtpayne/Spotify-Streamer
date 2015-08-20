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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final String KEY_TRACKS = "tracks";
    private final String KEY_MESSAGE = "message";
    private final String NO_TRACKS_FOUND_MESSAGE = "No tracks found. Please try another artist.";
    private final String NETWORK_NOT_AVAILABLE_MESSAGE = "Network is not available. Please try again later.";
    private final String SPOTIFY_NOT_AVAILABLE_MESSAGE = "We are experiencing issues. Please try again later.";

    private TrackAdapter mTrackAdapter;
    private TextView mMessageTextView;
    private String mMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Check Intent for artistId.
        String artistId = "";
        Bundle arguments = getArguments();
        if (arguments != null) {
            artistId = arguments.getString(Intent.EXTRA_TEXT);
        } else {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);
        ListView mTrackListView = (ListView) rootView.findViewById(R.id.listview_track);

        mTrackAdapter = new TrackAdapter(getActivity(), R.layout.listitem_track, new ArrayList<Track>());
        mTrackListView.setAdapter(mTrackAdapter);
        mMessageTextView = (TextView) rootView.findViewById(R.id.textview_message);

        // Check savedInstanceState for track list and message on orientation change.
        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey(KEY_TRACKS)) {
                mTrackAdapter.addAll(savedInstanceState.<Track>getParcelableArrayList(KEY_TRACKS));
            }
            mMessage = savedInstanceState.getString(KEY_MESSAGE);
            manageMessage();
        } else {
            // Default empty message
            mMessage = "";
            // Search for tracks.
            new SearchTrackTask().execute(artistId);
        }

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, "TEST");
                startActivity(intent);
                */
                FragmentManager fm = getFragmentManager();
                MusicPlayerActivityFragment dialogFragment = new MusicPlayerActivityFragment ();
                dialogFragment.show(fm, "Sample Fragment");
            }

        });

        return rootView;
    }

    /**
     * Checks for network availability.
     *
     * @return boolean true if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Manages display of texview message.
     * Clears list in event of network or SpotifyApi exceptions.
     */
    private void manageMessage() {
        // Set textview if message has changed
        if(!mMessageTextView.getText().toString().equalsIgnoreCase(mMessage)) {
            mMessageTextView.setText(mMessage);
        }

        if(mMessage.isEmpty()) {
            // Don't show empty message for spacing reasons
            mMessageTextView.setVisibility(View.GONE);
        } else {
            // Clear list if network or SpotifyApi errors
            if(mMessage.equalsIgnoreCase(NETWORK_NOT_AVAILABLE_MESSAGE)
                    || mMessage.equalsIgnoreCase(NETWORK_NOT_AVAILABLE_MESSAGE)) {
                mTrackAdapter.clear();
            }
            mMessageTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save track list if available.
        if (mTrackAdapter.getCount() > 0) {
            outState.putParcelableArrayList(KEY_TRACKS, mTrackAdapter.tracks);
        }
        outState.putString(KEY_MESSAGE, mMessage);
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

            Tracks tracks = null;

            if(isNetworkAvailable()) {
                // Use SpotifyApi to search for tracks.
                Map<String, Object> options = new HashMap<>();
                options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                try {
                    tracks = spotifyService.getArtistTopTrack(params[0], options);
                } catch (Exception e) {
                    // Display message if issues with SpotifyApi
                    mMessage = SPOTIFY_NOT_AVAILABLE_MESSAGE;
                }
            } else {
                // Display network not available message
                mMessage = NETWORK_NOT_AVAILABLE_MESSAGE;
            }
            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            if (tracks != null) {
                mTrackAdapter.clear();
                // Create a tracks list then add to adapter to prevent multiple refreshes
                List<Track> trackList = new ArrayList<>();
                for (kaaes.spotify.webapi.android.models.Track track : tracks.tracks) {
                    trackList.add(new Track(track));
                }
                mTrackAdapter.addAll(trackList);
                // Display message if no tracks returned from search.
                if(mTrackAdapter.isEmpty()) {
                    mMessage = NO_TRACKS_FOUND_MESSAGE;
                }
            }
            manageMessage();
        }

    }
}
