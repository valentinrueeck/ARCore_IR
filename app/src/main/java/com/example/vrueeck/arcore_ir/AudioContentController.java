package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.media.MediaPlayer;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;

public class AudioContentController {

    private MediaPlayer mediaPlayer;
    private Node playButtonNode;
    private Node pauseButtonNode;

    public AudioContentController(Context context, int resId){
        this.mediaPlayer = MediaPlayer.create(context, resId);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void playAudio() {
        this.mediaPlayer.start();
    }

    public void stopAudio() {
        this.mediaPlayer.stop();
    }

}
