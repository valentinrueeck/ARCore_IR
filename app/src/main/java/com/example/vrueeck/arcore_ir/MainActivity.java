package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity";
    public static final String IMAGE_DB_NAME = "referenceImages.imgdb";
    private Context context;
    private ArFragment arFragment;
    private Session arSession;
    private Config arConfig;
    private ArSceneView arSceneView;
    private Collection<AugmentedImage> lastUpdatedAugmentedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
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
            Log.d(LOG_TAG, "lastUpdatedImages: " + String.valueOf(lastUpdatedAugmentedImages.size()));
            for( AugmentedImage lastImage : lastUpdatedAugmentedImages){
                if( lastImage.getName().equals(image.getName())){
                    Log.d(LOG_TAG, image.getName() + " was already tracked");
                    break outerLoop;
                }
            }
            removeExistingAnchors();
            if (image.getTrackingState() == TrackingState.TRACKING) {
                Log.d(LOG_TAG, image.getName());
                Anchor imageAnchor = arSession.createAnchor(image.getCenterPose());
                AnchorNode anchorNode = new AnchorNode(imageAnchor);
                anchorNode.setName(image.getName() + "_anchorNode");
                anchorNode.setParent(arSceneView.getScene());

                ARContentCreator.addHighlightImagePlane(context, anchorNode, image);
                ARContentCreator.addInfoButton(context, anchorNode, image, arFragment);
                ARContentCreator.addPlayButton(context, anchorNode, image);
            }
            Log.d(LOG_TAG, "Anchors: " + arSession.getAllAnchors().size());
        }
        lastUpdatedAugmentedImages = augmentedImages;
    }

    public static void removeHighlightNode(Node highlightNodeParent, Node highlightNode) {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(() -> {
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
            InputStream inputStream = context.getAssets().open(IMAGE_DB_NAME);
            arConfig.setAugmentedImageDatabase(AugmentedImageDatabase.deserialize(arSession, inputStream));
            arSession.configure(arConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
