package com.mpayne.android.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.mpayne.android.spotifystreamer.Track;

import java.io.IOException;
import java.util.List;


public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String TAG = MusicPlayerService.class.getSimpleName();

    public static final String ACTION_TRACK_DETAIL = "action_track_detail";
    public static final String ACTION_TRACK_PLAY_STATUS = "action_track_play_status";

    public static final String ACTION_PLAY_TRACK = "action_play_track";
    public static final String ACTION_RESUME_TRACK = "action_resume_track";
    public static final String ACTION_PLAY_NEXT_TRACK = "action_play_next_track";
    public static final String ACTION_PLAY_PREVIOUS_TRACK = "action_play_previous_track";
    public static final String ACTION_PAUSE_TRACK = "action_pause_track";
    public static final String ACTION_CHANGE_TRACK_PROGRESS = "action_change_track_progress";
    public static final String ACTION_TRACK_PROGRESS = "action_track_progress";
    public static final String ACTION_TRACK_DURATION = "action_track_duration";

    public static final String EXTRA_ARTIST_NAME = "extra_artist_name";
    public static final String EXTRA_TRACK_LIST = "extra_track_list";
    public static final String EXTRA_TRACK = "extra_track";
    public static final String EXTRA_TRACK_IS_PLAYING = "extra_track_is_playing";
    public static final String EXTRA_TRACK_POSITION = "extra_track_position";
    public static final String EXTRA_TRACK_PROGRESS = "extra_track_progress";
    public static final String EXTRA_TRACK_DURATION = "extra_track_duration";

    private String mArtistName;

    private List<Track> mTrackList;
    private int mTrackPosition;
    private Track mTrack;

    private Handler mHandler;

    Intent mTrackDetailIntent = new Intent(ACTION_TRACK_DETAIL);
    Intent mTrackPlayStatusIntent = new Intent(ACTION_TRACK_PLAY_STATUS);
    Intent mTrackProgressIntent = new Intent(ACTION_TRACK_PROGRESS);
    Intent mTrackDurationIntent = new Intent(ACTION_TRACK_DURATION);

    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(sendBroadcasts);
        super.onDestroy();
    }

    private Runnable sendBroadcasts = new Runnable() {
        public void run() {
            broadcastTrackProgress();
            mHandler.postDelayed(this, 200);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_TRACK_DETAIL:
                broadcastTrackDetail();
                broadcastTrackPlayStatus();
                break;
            case ACTION_PLAY_TRACK:
                mArtistName = intent.getStringExtra(EXTRA_ARTIST_NAME);
                mTrackList = intent.getParcelableArrayListExtra(EXTRA_TRACK_LIST);
                mTrackPosition = intent.getIntExtra(EXTRA_TRACK_POSITION, -1);
                mTrack = mTrackList.get(mTrackPosition);
                playTrack();
                break;
            case ACTION_RESUME_TRACK:
                if(!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    broadcastTrackPlayStatus();
                    mHandler.post(sendBroadcasts);
                }
                break;
            case ACTION_PAUSE_TRACK:
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    broadcastTrackPlayStatus();
                    mHandler.removeCallbacks(sendBroadcasts);
                }
                break;
            case ACTION_PLAY_NEXT_TRACK:
                playNextTrack();
                break;
            case ACTION_PLAY_PREVIOUS_TRACK:
                playPreviousTrack();
                break;
            case ACTION_CHANGE_TRACK_PROGRESS:
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(intent.getIntExtra(EXTRA_TRACK_PROGRESS, -1));
                }
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // No need to broadcast progress while track isn't playing
        mHandler.removeCallbacks(sendBroadcasts);
        playNextTrack();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(mp != null) {
            // Start playing the track, broadcast progress and track details
            mp.start();
            mHandler.post(sendBroadcasts);
            broadcastTrackPlayStatus();
            broadcastTrackDetail();
        }
    }

    private void playTrack() {
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } else {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(mTrack.getPreviewUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextTrack() {
        if(mTrackPosition + 1 < mTrackList.size()) {
            mTrackPosition++;
            mTrack = mTrackList.get(mTrackPosition);
        } else {
            mTrackPosition = 0;
            mTrack = mTrackList.get(mTrackPosition);
        }
        playTrack();
    }

    private void playPreviousTrack() {
        if(mTrackList.size() == 1) {
            mTrackPosition = 0;
            mTrack = mTrackList.get(mTrackPosition);
        } else if(mTrackPosition == 0) {
            mTrackPosition = mTrackList.size() - 1;
            mTrack = mTrackList.get(mTrackPosition);
        } else {
            mTrackPosition--;
            mTrack = mTrackList.get(mTrackPosition);
        }
        playTrack();
    }

    private void broadcastTrackProgress() {
        mTrackProgressIntent.putExtra(EXTRA_TRACK_PROGRESS, mMediaPlayer.getCurrentPosition());
        sendLocalBroadcast(mTrackProgressIntent);
    }

    private void broadcastTrackPlayStatus() {
        mTrackPlayStatusIntent.putExtra(EXTRA_TRACK_IS_PLAYING, mMediaPlayer.isPlaying());
        sendLocalBroadcast(mTrackPlayStatusIntent);
    }

    private void broadcastTrackDetail() {
        mTrackDetailIntent.putExtra(EXTRA_ARTIST_NAME, mArtistName);
        mTrackDetailIntent.putExtra(EXTRA_TRACK, mTrack);
        sendLocalBroadcast(mTrackDetailIntent);
        // track may not be prepared yet
        if(mMediaPlayer.isPlaying()) {
            mTrackDurationIntent.putExtra(EXTRA_TRACK_DURATION, mMediaPlayer.getDuration());
            sendLocalBroadcast(mTrackDurationIntent);
        }
    }

    private void sendLocalBroadcast(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
