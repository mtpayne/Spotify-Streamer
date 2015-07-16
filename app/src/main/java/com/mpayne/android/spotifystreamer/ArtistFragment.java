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
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * Searches for and displays artists.
 */
public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private final String KEY_ARTIST = "artist";
    private final String KEY_SEARCH = "search";
    private final String KEY_MESSAGE = "message";
    private final String NO_RESULTS_FOUND_MESSAGE_PRE = "No results found for '";
    private final String NO_RESULTS_FOUND_MESSAGE_POST = "'. Please refine your search.";
    private final String NETWORK_NOT_AVAILABLE_MESSAGE = "Network is not available. Please try again later.";
    private final String SPOTIFY_NOT_AVAILABLE_MESSAGE = "We are experiencing issues. Please try again later.";


    private ArtistAdapter mArtistAdapter;
    private String mSearch;
    private TextView mMessageTextView;
    private String mMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
        mMessageTextView = (TextView) rootView.findViewById(R.id.textview_message);

        mArtistAdapter = new ArtistAdapter(getActivity(), R.layout.listitem_artist, new ArrayList<Artist>());
        listView.setAdapter(mArtistAdapter);

        // Check savedInstanceState for search text and artist list on orientation change
        if(savedInstanceState != null) {
            mSearch = savedInstanceState.getString(KEY_SEARCH);
            mMessage = savedInstanceState.getString(KEY_MESSAGE);

            Parcelable[] parcelables = savedInstanceState.getParcelableArray(KEY_ARTIST);
            if(parcelables != null) {
                for(Parcelable parcelable : parcelables) {
                    mArtistAdapter.add(((Artist) parcelable));
                }
            }
        } else {
            // Default is empty search and message
            mSearch = "";
            mMessage = "";
        }

        manageMessage();

        final SearchView searchView = (SearchView) rootView.findViewById(R.id.searchview_artist);
        searchView.setIconifiedByDefault(false);
        // Dynamically update artist list as user enters search keys.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Clear focus to remove keyboard and display list
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchArtist(newText);
                return true;
            }
        });

        // Set intent(artist.id) from list item click.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Artist artist = mArtistAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.id);
                intent.putExtra(Intent.EXTRA_TITLE, artist.name);
                startActivity(intent);
            }

        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save artist list and search key data if available.
        if (mArtistAdapter.getCount() > 0) {
            Parcelable[] parcelables = new Parcelable[mArtistAdapter.getCount()];
            for (int i = 0; i < mArtistAdapter.getCount(); i++) {
                parcelables[i] = mArtistAdapter.getItem(i);
            }
            outState.putParcelableArray(KEY_ARTIST, parcelables);
        }
        outState.putString(KEY_SEARCH, mSearch);
        outState.putString(KEY_MESSAGE, mMessage);
        super.onSaveInstanceState(outState);
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
            if(mMessageTextView.isShown()) {
                mMessageTextView.setVisibility(View.GONE);
            }
        } else {
            // Clear list if network or SpotifyApi errors
            if(mMessage.equalsIgnoreCase(NETWORK_NOT_AVAILABLE_MESSAGE)
                    || mMessage.equalsIgnoreCase(NETWORK_NOT_AVAILABLE_MESSAGE)) {
                mArtistAdapter.clear();
            }
            if(!mMessageTextView.isShown()) {
                mMessageTextView.setVisibility(View.VISIBLE);
            }
        }

    }

    private void searchArtist(String artist) {
        // Clear list if no search keys present.
        if (artist.isEmpty()) {
            mSearch = artist;
            mArtistAdapter.clear();
            // Remove visibility if message was being displayed
            mMessage = artist;
            manageMessage();
        } else {
            // Do new search only if keys have changed from previous search.
            if(!artist.equalsIgnoreCase(mSearch)) {
                new SearchArtistTask().execute(artist);
                mSearch = artist;
            }
        }
    }

    /**
     *  Background task for retrieving and populating artist list.
     */
    private class SearchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = SearchArtistTask.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {

            // Exit empty artist searches.
            if (params.length == 0) {
                return null;
            }

            ArtistsPager artistsPager = null;

            if(isNetworkAvailable()) {
                // Use SpotifyApi to search for artists.
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                try {
                    artistsPager = spotifyService.searchArtists(params[0]);
                } catch (Exception e) {
                    // Display message if issues with SpotifyApi
                    mMessage = SPOTIFY_NOT_AVAILABLE_MESSAGE;
                }
            } else {
                // Display network not available message
                mMessage = NETWORK_NOT_AVAILABLE_MESSAGE;
            }

            return artistsPager;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            // Prevent displaying list after mSearch has been cleared before previous process completes.
            if(mSearch.isEmpty()) {
                manageMessage();
                return;
            }

            if(artistsPager != null) {
                mArtistAdapter.clear();
                for(kaaes.spotify.webapi.android.models.Artist artist : artistsPager.artists.items) {
                    mArtistAdapter.add(new Artist(artist));
                }
                // If searching but no results. Need to show message.
                if(!mSearch.isEmpty() && mArtistAdapter.isEmpty()) {
                    mMessage = NO_RESULTS_FOUND_MESSAGE_PRE + mSearch + NO_RESULTS_FOUND_MESSAGE_POST;
                } else {
                    mMessage = "";
                }
            }

            manageMessage();

        }
    }
}
