/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samples.ocr;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.samples.ocr.camera.GraphicOverlay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    public interface TextDetectionCallback {
        void onAadhaarDetected(String text);
    }

    private GraphicOverlay<OcrGraphic> graphicOverlay;
    private TextDetectionCallback callback;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, TextDetectionCallback callback) {
        graphicOverlay = ocrGraphicOverlay;
        this.callback = callback;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {

                //*Adarsh Check for Aadhaar regex from the text detected
                if (!TextUtils.isEmpty(extractDigits(item.getValue()))) {
                    callback.onAadhaarDetected(item.getValue());
                    return;
                }
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                graphicOverlay.add(graphic);
            }
        }
    }

    private String extractDigits(String in) {
        in = in.replaceAll(" ", "");
        Pattern pattern = Pattern.compile("(\\d{12,12})");
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return matcher.group(0);
        } else return "";
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }
}
