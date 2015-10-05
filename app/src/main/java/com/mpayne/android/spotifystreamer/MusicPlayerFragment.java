package com.mpayne.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpayne.android.spotifystreamer.service.MusicPlayerService;
import com.squareup.picasso.Picasso;


/**
 * Communicates with MusicPlayerService to display currently playing information
 * and some basic media controls.
 */
public class MusicPlayerFragment extends DialogFragment {

    private final String TAG = MusicPlayerFragment.class.getSimpleName();

    TextView mArtistName;
    TextView mAlbumName;
    ImageView mAlbumImage;
    TextView mTrackName;
    SeekBar mTrackProgressBar;
    TextView mTrackProgress;
    TextView mTrackDuration;
    ImageButton mPlayButton;
    ImageButton mPauseButton;
    ImageButton mNextButton;
    ImageButton mPreviousButton;

    int mSeekBarProgress;

    private Artist mArtist;
    private Track mTrack;

    @Override
    public void onResume() {
        super.onResume();
        // Register to receive information from MusicPlayerService.
        IntentFilter intentFilter = new IntentFilter(MusicPlayerService.ACTION_TRACK_DETAIL);
        intentFilter.addAction(MusicPlayerService.ACTION_TRACK_PLAY_STATUS);
        intentFilter.addAction(MusicPlayerService.ACTION_TRACK_PROGRESS);
        intentFilter.addAction(MusicPlayerService.ACTION_TRACK_DURATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        mArtistName = (TextView) view.findViewById(R.id.textview_artist_name);
        mAlbumName = (TextView) view.findViewById(R.id.textview_album_name);
        mAlbumImage = (ImageView) view.findViewById(R.id.imageview_album_image);
        mTrackName = (TextView) view.findViewById(R.id.textview_track_name);
        mTrackProgressBar = (SeekBar)view.findViewById(R.id.seekbar_track_progress);
        mTrackProgress = (TextView) view.findViewById(R.id.textview_track_progress);
        mTrackDuration = (TextView) view.findViewById(R.id.textview_track_duration);
        mPlayButton = (ImageButton) view.findViewById(R.id.imagebutton_play);
        mPauseButton = (ImageButton) view.findViewById(R.id.imagebutton_pause);
        mNextButton = (ImageButton) view.findViewById(R.id.imagebutton_next);
        mPreviousButton = (ImageButton) view.findViewById(R.id.imagebutton_previous);


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resumeTrack();
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTrack();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextTrack();
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousTrack();
            }
        });

        mTrackProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Track progress if user moved bar.
                if (fromUser) {
                    mSeekBarProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Notify MusicPlayerService of change
                Intent intent = new Intent(getActivity(), MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_CHANGE_TRACK_PROGRESS);
                intent.putExtra(MusicPlayerService.EXTRA_TRACK_PROGRESS, mSeekBarProgress);
                getActivity().startService(intent);
            }
        });

        // Request currently playing information from MusicPlayerService.
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_TRACK_DETAIL);
        getActivity().startService(intent);

        return view;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case MusicPlayerService.ACTION_TRACK_DETAIL:
                    // Populate currently playing information
                    mArtistName.setText(intent.getStringExtra(MusicPlayerService.EXTRA_ARTIST_NAME));
                    mTrack = intent.getParcelableExtra(MusicPlayerService.EXTRA_TRACK);
                    mAlbumName.setText(mTrack.albumName);
                    if(null != mTrack.imageUrlLarge && Patterns.WEB_URL.matcher(mTrack.imageUrlLarge).matches()) {
                        Picasso.with(getActivity()).load(mTrack.imageUrlLarge).into(mAlbumImage);
                    }
                    mTrackName.setText(mTrack.name);
                    break;
                case MusicPlayerService.ACTION_TRACK_PLAY_STATUS:
                    // Toggle Play/Pause depending on playing status.
                    boolean trackIsPlaying = intent.getBooleanExtra(MusicPlayerService.EXTRA_TRACK_IS_PLAYING, false);
                    if(trackIsPlaying) {
                        mPlayButton.setVisibility(View.GONE);
                        mPauseButton.setVisibility(View.VISIBLE);
                    } else {
                        mPlayButton.setVisibility(View.VISIBLE);
                        mPauseButton.setVisibility(View.GONE);
                    }
                    break;
                case MusicPlayerService.ACTION_TRACK_PROGRESS:
                    // Set progress bar and timer
                    int progress = intent.getIntExtra(MusicPlayerService.EXTRA_TRACK_PROGRESS, 0);
                    mTrackProgressBar.setProgress(progress);
                    mTrackProgress.setText(String.format("%d:%02d", progress / (60000), (progress / 1000) % 60));
                    break;
                case MusicPlayerService.ACTION_TRACK_DURATION:
                    // Set track duration
                    int duration = intent.getIntExtra(MusicPlayerService.EXTRA_TRACK_DURATION, 0);
                    mTrackProgressBar.setMax(duration);
                    mTrackDuration.setText(String.format("%d:%02d", duration / (60000), (duration / 1000) % 60));
                    break;
            }
        }

    };

    private void resumeTrack() {
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_RESUME_TRACK);
        getActivity().startService(intent);
    }

    private void pauseTrack() {
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PAUSE_TRACK);
        getActivity().startService(intent);
    }

    private void playNextTrack() {
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PLAY_NEXT_TRACK);
        getActivity().startService(intent);
    }

    private void playPreviousTrack() {
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PLAY_PREVIOUS_TRACK);
        getActivity().startService(intent);
    }

}
