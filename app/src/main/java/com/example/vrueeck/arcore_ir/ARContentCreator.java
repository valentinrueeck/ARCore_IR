package com.example.vrueeck.arcore_ir;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ARContentCreator {

    public static ModelRenderable createHighlightImagePlane(AugmentedImage image, Context context) {
        Vector3 size = new Vector3(image.getExtentX(),image.getExtentZ(), -0.01f);
        Vector3 center = new Vector3(0,0,0);
        final Map<String, ModelRenderable> modelRenderableHashMap = new HashMap<>();


        MaterialFactory.makeTransparentWithColor(context,new Color(1,1,1,0.5f))
                .thenAccept(
                        material -> {
                            modelRenderableHashMap.put("highlightPlane",ShapeFactory.makeCube(size, center, material));
                        });

        return modelRenderableHashMap.get("highlightPlane");
    }

    public static ViewRenderable createDescriptionPlane(AugmentedImage image, Context context){
        final ViewRenderable[] textView = {null};

        CompletableFuture<ViewRenderable> future = ViewRenderable.builder().setView(context, R.layout.text_view).build();
        future.thenAccept( view -> {
            textView[0] = view;
//            ((TextView) textView[0].getView()).setText(PaintingDescriptionTextRetriever.retrieveDescriptionText(image.getName()));
//            ((TextView)textView[0].getView().findViewById(R.id.textView)).setText(PaintingDescriptionTextRetriever.retrieveDescriptionText(image.getName()));
            Log.d("IMAGE", "ViewRenderable is ready");
        });

        return textView[0];
    }

    private static TextView createTextView(AugmentedImage image, Context context){
        TextView textView = new TextView(context);
        textView.setText(PaintingDescriptionTextRetriever.retrieveDescriptionText(image.getName()));
        return textView;
     }
}
