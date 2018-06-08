package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioContentController {

    private MediaPlayer mediaPlayer;

    AudioContentController(){}

    public MediaPlayer getMediaPlayer() {
        if (this.mediaPlayer != null) {
            return mediaPlayer;
        }
        return null;
    }

    public void setUpMediaPlayer(Context context, int resId){
        this.mediaPlayer = MediaPlayer.create(context, resId);
    }

    public void playAudio() {
        if(this.mediaPlayer != null){
            this.mediaPlayer.start();
        }
    }

    public void stopAudio() {
        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
        }
    }

    public boolean isAudioPlaying() {
        return mediaPlayer != null && this.mediaPlayer.isPlaying();
    }

}
