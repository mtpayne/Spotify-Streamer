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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity implements  ArtistFragment.Callback {

    private final String TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTwoPane = getResources().getBoolean(R.bool.two_pane);
        if(savedInstanceState == null) {
            if(mTwoPane) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_track, new TrackFragment(), TrackFragment.class.getSimpleName())
                        .commit();
            }
        }
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

            TrackFragment fragment = new TrackFragment();
            fragment.setArguments(args);

            /*
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_track, fragment, TrackFragment.class.getSimpleName())
                    .commit();
            */

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_track, fragment, TrackFragment.class.getSimpleName());
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, artist.id);
            intent.putExtra(Intent.EXTRA_TITLE, artist.name);
            startActivity(intent);
        }
    }

}
