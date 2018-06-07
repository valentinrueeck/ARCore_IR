package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.media.MediaPlayer;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;

public class AudioContentController {

    private MediaPlayer mediaPlayer;


    AudioContentController(){}

    public MediaPlayer getMediaPlayer() {
        if (this.mediaPlayer != null) {
            return mediaPlayer;
        }
        return null;
    }

    public void setUpAudio(Context context, int resId){
        this.mediaPlayer = MediaPlayer.create(context, resId);
    }

    public void playAudio() {
        this.mediaPlayer.start();
    }

    public void stopAudio() {
        this.mediaPlayer.stop();
    }

    public boolean isAudioPlaying(){
        return this.mediaPlayer.isPlaying();
    }

}
