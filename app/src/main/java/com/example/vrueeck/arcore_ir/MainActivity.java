package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.HandMotionAnimation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.CancelledKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private ArFragment arFragment;
    private Session arSession;
    private AugmentedImageDatabase augmentedImageDatabase;
    private Config arConfig;
    private ArSceneView arSceneView;
    private Handler handler = new Handler();
    private Collection<AugmentedImage> lastUpdatedAugmentedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();
        try {
            arSession = new Session(context);
        } catch (UnavailableArcoreNotInstalledException | UnavailableApkTooOldException | UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }
        arConfig = new Config(arSession);
        arConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        arConfig.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        setUpImageDb();
        arSceneView.setupSession(arSession);
        lastUpdatedAugmentedImages = new ArrayList<>();

        arSceneView.getScene()
                .setOnUpdateListener(frameTime -> {
                    Frame frame = arSceneView.getArFrame();
                    if (frame == null) {
                        return;
                    }
                    Collection<AugmentedImage> augmentedImages = frame
                            .getUpdatedTrackables(AugmentedImage.class);
                    if (augmentedImages != null) {
                        if(augmentedImages.size() > 0 ) {
                            handleReferenceImages(augmentedImages);
                        }
                    }
                });
    }


    private void handleReferenceImages(Collection<AugmentedImage> augmentedImages) {
        outerLoop:
        for (AugmentedImage image : augmentedImages) {
            Log.d("IMAGE", "lastUpdatedImages: " + String.valueOf(lastUpdatedAugmentedImages.size()));
            for( AugmentedImage lastImage : lastUpdatedAugmentedImages){
                if( lastImage.getName().equals(image.getName())){
                    Log.d("IMAGE", image.getName() + " was already tracked");
                    break outerLoop;
                }
            }
            removeExistingAnchors();
            if (image.getTrackingState() == TrackingState.TRACKING) {
                Log.d("IMAGE_NAME: ", image.getName());
                Anchor imageAnchor = arSession.createAnchor(image.getCenterPose());
                AnchorNode anchorNode = new AnchorNode(imageAnchor);
                anchorNode.setName(image.getName() + "_anchorNode");
                anchorNode.setParent(arSceneView.getScene());

                ModelRenderable highlightPlane = ARContentCreator.createHighlightImagePlane(image, context);
                Node highlightPlaneNode = new Node();
                highlightPlaneNode.setName("highlightPlaneNode");
                highlightPlaneNode.setRenderable(highlightPlane);
                highlightPlaneNode.setLocalRotation(new Quaternion(new Vector3(1,0,0), 90));
                highlightPlaneNode.setParent(anchorNode);
                removeHighlightNode(anchorNode, highlightPlaneNode);

//                addNodeToScene(image, anchorNode);
                addInfoButton(anchorNode, image);


                Collection<Node> nodes = arSceneView.getScene().getChildren();
                Node n = new Node();


            }
            Log.d("IMAGE", "Anchors: " + arSession.getAllAnchors().size());
        }
        lastUpdatedAugmentedImages = augmentedImages;
    }

    private void removeHighlightNode(Node highlightNodeParent, Node highlightNode) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        handler.post(() -> {
                            Log.d("TimeOut", "f");
                            highlightNodeParent.removeChild(highlightNode);
                        });
                    }
                },
                500
        );
    }

    private void removeExistingAnchors(){
        for (Anchor anchor : arSession.getAllAnchors()){
            anchor.detach();
        }
        lastUpdatedAugmentedImages = Collections.emptyList();
    }

    private void setUpImageDb() {
        try {
            InputStream inputStream = context.getAssets().open("referenceImages.imgdb");
            augmentedImageDatabase = new AugmentedImageDatabase(arSession);
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(arSession, inputStream);
            arConfig.setAugmentedImageDatabase(augmentedImageDatabase);
            arSession.configure(arConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNodeToScene(AugmentedImage image, AnchorNode anchorNode){
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
            descriptionPlaneNode.setName("descriptionPlaneNode");
            descriptionPlaneNode.setRenderable(view);
            descriptionPlaneNode.setLocalRotation(new Quaternion(new Vector3(1,0,0), -90));
            descriptionPlaneNode.setLocalPosition(new Vector3(0f,-image.getExtentZ(),0f));
            descriptionPlaneNode.setParent(anchorNode);
        });
    }

    private void addInfoButton(AnchorNode anchorNode, AugmentedImage image) {
        ModelRenderable.builder()
                .setSource(this,R.raw.infobuttonobj)
                .build()
                .thenAccept( infoButton -> {
                    Node infoButtonNode = new Node();
                    infoButtonNode.setName("infoButtonNode");
                    infoButtonNode.setRenderable(infoButton);
                    infoButtonNode.setWorldScale(new Vector3(0.02f,0.02f,0.02f));
                    infoButtonNode.setLocalRotation(new Quaternion(new Vector3(1f,0f,0f), -90));
                    infoButtonNode.setParent(anchorNode);
                    infoButtonNode.setLocalPosition(new Vector3(0f, image.getExtentZ() / 2, 0f));
                    infoButtonNode.setOnTapListener((hitTestResult, motionEvent) -> {
                        Log.d("TAP: ", "infoButtonNode tapped");
                        addNodeToScene(image, anchorNode);
                    });
                })
                .exceptionally(
                        throwable -> {
                            Log.e("RRR", "Unable to load Renderable.", throwable);
                            return null;
                        });
    }


}
