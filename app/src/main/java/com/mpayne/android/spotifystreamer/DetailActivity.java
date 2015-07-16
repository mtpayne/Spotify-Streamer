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
import android.support.v7.app.ActionBarActivity;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // Check Intent for artist name.
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TITLE)) {
            String artistName = intent.getStringExtra(Intent.EXTRA_TITLE);
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                getSupportActionBar().setSubtitle(artistName);
            }
        }
    }

}
