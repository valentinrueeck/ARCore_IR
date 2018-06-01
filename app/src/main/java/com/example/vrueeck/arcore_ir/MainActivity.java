package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private ArFragment arFragment;
    private Session arSession;
    private AugmentedImageDatabase augmentedImageDatabase;
    private Config arConfig;
    private ArSceneView arSceneView;
    private List<Anchor> sessionAnchors;

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
        setUpImageDb();
        arSceneView.setupSession(arSession);
        sessionAnchors = new ArrayList<>();

        arSceneView.getScene()
                .setOnUpdateListener(frameTime -> {
                    Frame frame = arSceneView.getArFrame();
                    if (frame == null) {
                        return;
                    }
                    Collection<AugmentedImage> augmentedImages = frame
                            .getUpdatedTrackables(AugmentedImage.class);
                    if (augmentedImages != null) {
                        handleReferenceImages(augmentedImages);
                    }

                });

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

    private void handleReferenceImages(Collection<AugmentedImage> augmentedImages) {
        for (AugmentedImage image : augmentedImages) {
            if (image.getTrackingState() == TrackingState.TRACKING) {
                Log.d("IMAGE_NAME: ", image.getName());
                Anchor imageAnchor = arSession.createAnchor(image.getCenterPose());
                sessionAnchors.add(imageAnchor);

                AnchorNode anchorNode = new AnchorNode(imageAnchor);
                anchorNode.setName(image.getName() + "_anchorNode");
                anchorNode.setParent(arSceneView.getScene());
                Log.d("ANCHORS: ", String.valueOf(arSession.getAllAnchors().size()));

                ModelRenderable highlightPlane = ARContentCreator.createHighlightImagePlane(image, context);
                Node highlightPlaneNode = new Node();
                highlightPlaneNode.setName("highlightPlaneNode");
                highlightPlaneNode.setRenderable(highlightPlane);
                highlightPlaneNode.setLocalRotation(new Quaternion(new Vector3(1,0,0), 90));
                highlightPlaneNode.setParent(anchorNode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeHighlightNode(anchorNode, highlightPlaneNode);
                    }
                });
            }
        }
    }

    private void removeHighlightNode(Node highlightNodeParent, Node highlightNode) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Log.d("TimeOut", "f");
                        highlightNodeParent.removeChild(highlightNode);
                    }
                },
                1500
        );
    }


}
