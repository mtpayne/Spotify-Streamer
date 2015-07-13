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

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Defines track information for display and implements Parcelable to retain data on screen rotation.
 */
public class Track implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Track createFromParcel(Parcel in ) {
            return new Track( in );
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    String name;
    String albumName;
    String imageUrlSmall;
    String imageUrlLarge;

    /**
     * Constructor taking in kaaes.spotify.webapi.android.models.Track
     * and extracting relevant data for displaying in this app.
     */
    public Track(kaaes.spotify.webapi.android.models.Track track) {
        super();
        this.name = track.name;
        this.albumName = track.album.name;
        if (!track.album.images.isEmpty()) {
            if(track.album.images.size() > 1)
            {
                // Iterate and use two smallest image sizes
                int image1Dimension = 0;
                int image2Dimension = 0;
                int temp;
                for (Image image : track.album.images) {
                    temp = image.height * image.width;
                    // set image1Dimension first time
                    if(image1Dimension == 0) {
                        image1Dimension = temp;
                        imageUrlSmall = image.url;
                    }
                    // set image1Dimension to smallest possible
                    if(temp < image1Dimension) {
                        image1Dimension = temp;
                        imageUrlSmall = image.url;
                    }
                    // set image2Dimension to smallest possible but larger than image1Dimension
                    if(temp > image1Dimension) {
                        // set image2Dimension first time
                        if(image2Dimension == 0) {
                            image2Dimension = temp;
                            imageUrlLarge = image.url;
                        }
                        if(temp < image2Dimension) {
                            image2Dimension = temp;
                            imageUrlLarge = image.url;
                        }
                    }
                }
            } else {
                imageUrlSmall = track.album.images.get(0).url;
                imageUrlLarge = imageUrlSmall;
            }

        }
    }

    public Track(Parcel in) {
        name = in.readString();
        albumName = in.readString();
        imageUrlSmall = in.readString();
        imageUrlLarge = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(albumName);
        dest.writeString(imageUrlSmall);
        dest.writeString(imageUrlLarge);
    }


}
