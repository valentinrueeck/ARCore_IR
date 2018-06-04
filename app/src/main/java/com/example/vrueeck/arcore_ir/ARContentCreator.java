package com.example.vrueeck.arcore_ir;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;


public class ARContentCreator {

    public static ModelRenderable createHighlightImagePlane(AugmentedImage image, Context context) {
        Vector3 size = new Vector3(image.getExtentX(),image.getExtentZ(), -0.01f);
        Vector3 center = new Vector3(0,0,0);
        final ModelRenderable[] highlightPlane = {null};

        MaterialFactory.makeTransparentWithColor(context,new Color(1,1,1,0.5f))
                .thenAccept(
                        material -> {
                            highlightPlane[0] = ShapeFactory.makeCube(size, center, material);
                        });
        return highlightPlane[0];
    }


}
