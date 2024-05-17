package org.acme;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FaceDetectionService {

    @Inject
    Predictor<Image, DetectedObjects> predictor;

    public DetectedObjects detectFaces(Image img) throws TranslateException {
        return predictor.predict(img);
    }

}
