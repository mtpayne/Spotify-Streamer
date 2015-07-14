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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
    private final String NO_RESULTS_FOUND_MESSAGE_PRE = "No results found for '";
    private final String NO_RESULTS_FOUND_MESSAGE_POST = "'. Please refine your search.";

    private ArtistAdapter mArtistAdapter;
    private String mSearch;
    private TextView mMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
        mMessage = (TextView) rootView.findViewById(R.id.textview_message);

        mArtistAdapter = new ArtistAdapter(getActivity(), R.layout.listitem_artist, new ArrayList<Artist>());
        listView.setAdapter(mArtistAdapter);

        // Start with empty search.
        mSearch = "";

        // Check savedInstanceState for search text and artist list on orientation change
        if(savedInstanceState != null) {
            String search = savedInstanceState.getString(KEY_SEARCH);
            if(search != null) {
                mSearch = search;
            }
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(KEY_ARTIST);
            if(parcelables != null) {
                for(Parcelable parcelable : parcelables) {
                    mArtistAdapter.add(((Artist) parcelable));
                }
            }
            // If searching but no results need to show message.
            if(!mSearch.isEmpty() && mArtistAdapter.isEmpty()) {
                mMessage.setText(NO_RESULTS_FOUND_MESSAGE_PRE + mSearch + NO_RESULTS_FOUND_MESSAGE_POST);
                mMessage.setVisibility(View.VISIBLE);
            }
        } else {
            // Don't show empty message for spacing reasons.
            mMessage.setVisibility(View.GONE);
        }

        // Dynamically update artist list as user enters search keys.
        final EditText editText = (EditText) rootView.findViewById(R.id.edittext_search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear list if no search keys present.
                if (s.toString().isEmpty()) {
                    mArtistAdapter.clear();
                    mSearch = s.toString();
                } else {
                    // Do new search only if keys have changed from previous search.
                    if(!s.toString().equalsIgnoreCase(mSearch)) {
                        new SearchArtistTask().execute(s.toString());
                        mSearch = s.toString();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Set intent(artist.id) from list item click.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Artist artist = mArtistAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.id);
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
        super.onSaveInstanceState(outState);
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

            // Use SpotifyApi to search for artists.
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();

            return spotifyService.searchArtists(params[0]);
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            // Prevent displaying list after mSearch has been cleared before process completes.
            if(mSearch.isEmpty()) {
                mMessage.setVisibility(View.GONE);
                return;
            }

            if(artistsPager != null) {
                mArtistAdapter.clear();
                for(kaaes.spotify.webapi.android.models.Artist artist : artistsPager.artists.items) {
                    mArtistAdapter.add(new Artist(artist));
                }
                //mArtistAdapter.notifyDataSetChanged();
            }

            // Display message if no artists are returned from search
            if(!mSearch.isEmpty() && mArtistAdapter.getCount() == 0) {
                mMessage.setText(NO_RESULTS_FOUND_MESSAGE_PRE + mSearch + NO_RESULTS_FOUND_MESSAGE_POST);
                mMessage.setVisibility(View.VISIBLE);
            } else {
                // Don't display empty message space.
                mMessage.setVisibility(View.GONE);
            }
        }
    }
}
