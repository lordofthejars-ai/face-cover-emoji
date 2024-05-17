package org.acme;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.translate.TranslateException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AgeDetectionService {

    @Inject
    Predictor<Image, Classifications> predictor;

    public Classifications age(Image img) throws TranslateException {
        return predictor.predict(img);
    }
}
