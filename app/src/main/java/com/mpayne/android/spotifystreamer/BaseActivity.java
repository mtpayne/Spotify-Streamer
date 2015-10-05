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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;

import com.mpayne.android.spotifystreamer.service.MusicPlayerService;

import java.util.ArrayList;


/**
 * Activity to be extended that handles common functionality.
 */
public class BaseActivity extends ActionBarActivity implements  ArtistFragment.Callback, TrackFragment.Callback {

    // Identifier for two-pane layout.
    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTwoPane = getResources().getBoolean(R.bool.two_pane);
    }

    @Override
    public void onArtistSelected(Artist artist) {
        if(mTwoPane) {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                getSupportActionBar().setSubtitle(artist.name);
            }

            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, artist.id);
            args.putString(Intent.EXTRA_TITLE, artist.name);
            args.putParcelable(Artist.class.getSimpleName(), artist);

            TrackFragment fragment = new TrackFragment();
            fragment.setArguments(args);

            // Replace fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_track, fragment, TrackFragment.class.getSimpleName())
                    .commit();

            /*
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_track, fragment, TrackFragment.class.getSimpleName());
            transaction.addToBackStack(null);
            transaction.commit();
            */

        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, artist.id);
            intent.putExtra(Intent.EXTRA_TITLE, artist.name);
            intent.putExtra(Artist.class.getSimpleName(), artist);
            startActivity(intent);
        }
    }

    @Override
    public void onTrackSelected(Artist artist, ArrayList<Track> tracks, int position) {
        // Start MusicPlayerService
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PLAY_TRACK);
        intent.putExtra(MusicPlayerService.EXTRA_ARTIST_NAME, artist.name);
        intent.putParcelableArrayListExtra(MusicPlayerService.EXTRA_TRACK_LIST, tracks);
        intent.putExtra(MusicPlayerService.EXTRA_TRACK_POSITION, position);
        startService(intent);

        if(mTwoPane) {
            // Show as DialogFragment
            DialogFragment dialogFragment = new MusicPlayerFragment();
            dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialogFragment.show(getSupportFragmentManager(), MusicPlayerFragment.class.getSimpleName());
        } else {
            // Call activity
            Intent musicPlayerActivityIntent = new Intent(this, MusicPlayerActivity.class);
            startActivity(musicPlayerActivityIntent);
        }
    }

}