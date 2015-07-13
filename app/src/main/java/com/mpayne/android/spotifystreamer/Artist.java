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
 * Defines artist information for display and implements Parcelable to retain data on screen rotation.
 */
public class Artist implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Artist createFromParcel(Parcel in ) {
            return new Artist( in );
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    String name;
    String id;
    String imageUrlSmall;
    //List<String> trackNames;
    String imageUrlLarge;

    /**
     * Constructor taking in kaaes.spotify.webapi.android.models.Artist
     * and extracting relevant data for displaying in this app.
     */
    public Artist(kaaes.spotify.webapi.android.models.Artist artist) {
        super();
        name = artist.name;
        id = artist.id;
        if (!artist.images.isEmpty()) {
            if(artist.images.size() > 1)
            {
                // Iterate and use two smallest image sizes
                //imageUrlSmall
                int image1Dimension = 0;
                int image2Dimension = 0;
                int temp;
                for (Image image : artist.images) {
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
                imageUrlSmall = artist.images.get(0).url;
                imageUrlLarge = imageUrlSmall;
            }

        } else {
            //grey_square image
            //imageUrlSmall = R.drawable.grey_square;
            //imageUrlLarge = R.drawable.grey_square;
        }
    }

    public Artist(Parcel in) {
        name = in.readString();
        id = in.readString();
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
        dest.writeString(id);
        dest.writeString(imageUrlSmall);
        dest.writeString(imageUrlLarge);
    }

}
