package com.example.vrueeck.arcore_ir;

import android.content.Context;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

public class ARContentCreator {
    public static final String LOG_TAG = "ARContenCreator";
    public static final String HIGHLIGHT_PLANE_NODE_NAME = "highlightPlaneNode";
    public static final String DESCRIPTION_PLANE_NODE_NAME = "descriptionPlaneNode";
    public static final String INFO_BUTTON_NODE_NAME = "infoButtonNode";
    public static final String PLAY_BUTTON_NODE_NAME = "playButtonNode";
    public static final String PAUSE_BUTTON_NODE_NAME = "pauseButtonNode";
    private static AudioContentController audioContentController;


    public static void addHighlightImagePlane(Context context, AnchorNode anchorNode, AugmentedImage image) {
        Vector3 size = new Vector3(image.getExtentX(),image.getExtentZ(), -0.01f);
        Vector3 center = new Vector3(0,0,0);


        MaterialFactory.makeTransparentWithColor(context,new Color(1,1,1,0.5f))
                .thenAccept(
                        material -> {
                            ModelRenderable highlightPlane = ShapeFactory.makeCube(size, center, material);
                            Node highlightPlaneNode = new Node();
                            highlightPlaneNode.setName(HIGHLIGHT_PLANE_NODE_NAME);
                            highlightPlaneNode.setRenderable(highlightPlane);
                            highlightPlaneNode.setLocalRotation(new Quaternion(new Vector3(1,0,0), 90));
                            highlightPlaneNode.setParent(anchorNode);
                            MainActivity.removeHighlightNode(anchorNode, highlightPlaneNode);
                        });
    }

    public static void addDescriptionPlane(Context context, AnchorNode anchorNode, AugmentedImage image){
        CompletableFuture<ViewRenderable> future = ViewRenderable.builder().setView(context, R.layout.text_view).build();
        future.thenAccept( view -> {
            int height = Math.round(view.getPixelsToMetersRatio() * 0.25f);
            int width = Math.round(view.getPixelsToMetersRatio() * image.getExtentX());

            TextView headline = view.getView().findViewById(R.id.textViewHeadline);
            SpannableString headlineText = new SpannableString(image.getName());
            headlineText.setSpan(new UnderlineSpan(),0, headlineText.length(),0);
            headline.setText(headlineText);

            TextView descriptionText = view.getView().findViewById(R.id.textView);
            descriptionText.setText(PaintingDescriptionTextRetriever.retrieveDescriptionText(image.getName()));
            descriptionText.setWidth(width);
            descriptionText.setHeight(height);

            Node descriptionPlaneNode = new Node();
            descriptionPlaneNode.setName(DESCRIPTION_PLANE_NODE_NAME);
            descriptionPlaneNode.setRenderable(view);
            descriptionPlaneNode.setParent(anchorNode);
            descriptionPlaneNode.setLocalPosition(new Vector3(0f,0f, image.getExtentZ() + descriptionText.getHeight() + 0.3f));
            descriptionPlaneNode.setLocalRotation(new Quaternion(new Vector3(1,0,0), -90));
        });
    }

    public static void addInfoButton(Context context,AnchorNode anchorNode, AugmentedImage image) {
        ModelRenderable.builder()
                .setSource(context, R.raw.infobutton)
                .build()
                .thenAccept( infoButton -> {
                    Node infoButtonNode = new Node();
                    infoButtonNode.setName(INFO_BUTTON_NODE_NAME);
                    infoButtonNode.setRenderable(infoButton);
                    infoButtonNode.setWorldScale(new Vector3(0.005f,0.005f,0.005f));
                    infoButtonNode.setLocalRotation(new Quaternion(new Vector3(1f,0f,0f), -90));
                    infoButtonNode.setParent(anchorNode);
                    infoButtonNode.setLocalPosition(new Vector3(0f,0f, -image.getExtentZ() / 2 - infoButtonNode.getWorldScale().x / 2));
                    infoButtonNode.setOnTapListener((hitTestResult, motionEvent) -> {
                        Log.d(LOG_TAG, "infoButtonNode tapped");
                        ARContentCreator.addDescriptionPlane(context, anchorNode, image);
                    });
                })
                .exceptionally(
                        throwable -> {
                            Log.e(LOG_TAG, "Unable to load Renderable.", throwable);
                            return null;
                        });
    }

    public static void addPlayButton(Context context, AnchorNode anchorNode, AugmentedImage image){
        ModelRenderable.builder()
                .setSource(context, R.raw.playbutton)
                .build()
                .thenAccept( playButton -> {
                    Node playButtonNode = new Node();
                    playButtonNode.setName(PLAY_BUTTON_NODE_NAME);
                    playButtonNode.setRenderable(playButton);
                    playButtonNode.setWorldScale(new Vector3(0.002f,0.002f,0.002f));
                    playButtonNode.setLocalRotation(new Quaternion(new Vector3(0f,1f,0f), -90));
                    playButtonNode.setParent(anchorNode);
                    playButtonNode.setLocalPosition(new Vector3(image.getExtentX() / 2 + playButtonNode.getWorldScale().x, 0f, 0f));
                    playButtonNode.setOnTapListener((hitTestResult, motionEvent) -> {
                        audioContentController = new AudioContentController(context, getAudioFileResId(context, image.getName().toLowerCase()));
                        audioContentController.playAudio();
                        audioContentController.getMediaPlayer().setOnCompletionListener(mp -> addPlayButton(context, anchorNode,image));
                        anchorNode.removeChild(playButtonNode);
                        ARContentCreator.addPauseButtonNode(context, anchorNode, image);
                    });
                })
                .exceptionally(
                        throwable -> {
                            Log.e(LOG_TAG, "Unable to load Renderable.", throwable);
                            return null;
                        });
    }

    private static void addPauseButtonNode(Context context, AnchorNode anchorNode, AugmentedImage image){
        ModelRenderable.builder()
                .setSource(context, R.raw.pausebutton)
                .build()
                .thenAccept( pauseButton -> {
                    Node pauseButtonNode = new Node();
                    pauseButtonNode.setName(PAUSE_BUTTON_NODE_NAME);
                    pauseButtonNode.setRenderable(pauseButton);
                    pauseButtonNode.setWorldScale(new Vector3(0.002f,0.002f,0.002f));
                    pauseButtonNode.setLocalRotation(new Quaternion(new Vector3(0f,1f,0f), -90));
                    pauseButtonNode.setParent(anchorNode);
                    pauseButtonNode.setLocalPosition(new Vector3(image.getExtentX() / 2 + pauseButtonNode.getWorldScale().x / 2, 0f, 0f));
                    pauseButtonNode.setOnTapListener((hitTestResult, motionEvent) -> {
                        audioContentController.stopAudio();
                        anchorNode.removeChild(pauseButtonNode);
                        ARContentCreator.addPlayButton(context, anchorNode, image);

                    });
                })
                .exceptionally(
                        throwable -> {
                            Log.e(LOG_TAG, "Unable to load Renderable.", throwable);
                            return null;
                        });
    }

    private static int getAudioFileResId(Context context, String imageName){
        return context.getResources().getIdentifier(imageName + "_audio", "raw", context.getPackageName());
    }

}
