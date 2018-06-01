package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private ArFragment arFragment;
    private Session arSession;
    private AugmentedImageDatabase augmentedImageDatabase;
    private Config arConfig;
    private ArSceneView arSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();
        arSession = arSceneView.getSession();
        arConfig = new Config(arSession);

        setUpImageDb();
    }


    private void setUpImageDb(){
        try {
            InputStream inputStream = context.getAssets().open("referenceImages.imgdb");
            augmentedImageDatabase = new AugmentedImageDatabase(arSession);
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(arSession,inputStream);
            arConfig.setAugmentedImageDatabase(augmentedImageDatabase);
            arSession.configure(arConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//        arSceneView.getScene()
//                .setOnUpdateListener(
//            frameTime -> {
//        Frame frame = arSceneView.getArFrame();
//        if (frame == null) {
//            return;
//        }
//
//        Collection<AugmentedImage> augmentedImages = frame
//                .getUpdatedTrackables(AugmentedImage.class);
//    });

}
